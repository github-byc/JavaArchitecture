# 基类蓝图(core 模块)

新项目 core 模块按本文档生成**等价代码**,包根 `${基础包名}.core`,顶层四包:`common` / `framework` / `middleware` / `business`。本文档是基类契约的唯一权威来源,方法签名与行为语义一律以此为准,不要凭记忆改动或臆测。

---

## common(通用基础)

### Result(`common.domain`)

若依 AjaxResult 兼容格式(前端为若依系),`extends HashMap<String,Object>`,固定 key:`code` / `msg` / `data`(data 为 null 时不放入)。

- 静态工厂:`success()` / `success(data)` / `success(msg)` / `success(msg, data)` → code=200
- `warn(msg[, data])` → code=601(系统警告)
- `error()` / `error(msg)` / `error(msg, data)` → code=400;`error(code, msg)` 自定义
- `isSuccess()` / `isWarn()` / `isError()`;覆写 `put` 支持链式

### 异常(`common.exception`)

- `BaseException extends RuntimeException`:全参构造族(无参/message/message+cause/cause/完整)
- `BusinessException`、`ExcelException`(可携带 `data`,通常是 `List<ErrorMessage>`)、`MQException` 均继承 BaseException

### 全局异常处理器(`common.web.global.GlobalExceptionHandler`)

| 异常 | HTTP | 行为 |
|---|---|---|
| BindException / MethodArgumentNotValidException | 400 | 所有校验消息 `;` 拼接为 msg |
| ExcelException | 400 | message + data |
| BusinessException | 400 | message + datas |
| RuntimeException / Exception | 500 | message |

统一返回 `ResponseEntity<Result>`,并 `StaticLog.error` 记录 URI、异常类名、消息。默认实现 `DefaultGlobalExceptionHandler`(@RestControllerAdvice)随 core 自动装配注册;500 系异常不回传原始消息(固定兜底文案,防信息泄露),另有 DuplicateKeyException → 400 唯一键冲突提示。业务模块可继承 GlobalExceptionHandler 定制后替换本 Bean。

### 校验分组(`common.validation`)

`CreateGroup` / `UpdateGroup` 空标记接口。DTO 的 id 上 `@NotBlank(message = "主键不能为空", groups = {UpdateGroup.class})`。

### 上下文与横切(`common.context` / `common.interceptor`)

- `SecurityContextHolder`:`ThreadLocal<AuthSecurityUser>`;`getContext/setContext/clearContext`;`getCurrentUsername()` — MQ 消费场景优先取 `operateMqUserName`,否则取登录用户名。拦截器入口必须 `clearContext` 防内存泄漏。
- `TransactionalEvent`:事务提交后才发布的事件封装。
- `TraceIdInterceptor`:链路追踪(TraceId 注入日志/响应)。
- `GlobalLogInfo`:全局日志文案常量。

### 通用 DTO 与模型(`common.domain.dto` / `model` / `function`)

- `BaseIdDTO { @NotBlank String id }`、`BaseIdsDTO { @NotEmpty Set<String> ids }`
- `AuthLoginUser` / `AuthSecurityUser`(登录与安全上下文用户模型)
- `Tuple2` / `Tuple3`(轻量元组)

---

## framework/crud(泛型 CRUD 框架,核心中的核心)

### 每实体一条继承链

```
XxxEntity → XxxDao → XxxService(接口) → XxxServiceImpl → XxxController
        + XxxDTO / XxxQueryDTO / XxxVO (/ XxxUploadDTO)
```

- `BaseDao<T> extends MPJBaseMapper<T>`(mybatis-plus-join,天然支持连表)

### BaseEntity(`framework.crud.entity`)

abstract,实现 Serializable,`@Getter @Setter`:

| 字段 | 列 | 说明 |
|---|---|---|
| String id | id | @TableId("id"),业务不生成、框架填 |
| String createdBy | created_by | 创建者 |
| LocalDateTime createdOn | created_on | 创建时间 |
| String modifiedBy | modified_by | 修改者 |
| LocalDateTime modifiedOn | modified_on | 修改时间 |
| Integer version = 0 | version | @Version 乐观锁 |

全部字段带 `@Schema(description=...)`。**每张业务表必含这六列**(snake_case)。
id 由 `IdGenerator`(common.util)生成——雪花算法,可配 `goldlion.id.worker-id`/`goldlion.id.datacenter-id` 消除容器化多实例主键重复风险,未配置回退 MAC/PID 推导。

### BaseDTO / QueryDTO / BaseVO

- `BaseDTO`:`id`,`@NotBlank(groups = UpdateGroup.class)`
- `QueryDTO`:
  - `List<Condition> queryCondition`(默认空表)— 前端动态筛选;`Condition { String columnName; String sqlKeyword; Object value; Object value2; }`,sqlKeyword 取值 = `SqlKeyword` 的 SQL 片段(`=`、`<>`、`>`、`>=`、`<`、`<=`、`LIKE`、`IN`(value 逗号拼接)、`BETWEEN`(用 value+value2))
  - `String sortName` + `String sortType`(默认 `DESC`)
  - `boolean cache = true`(是否走缓存实现)
- `BaseVO`:`id`、`version`、`createdBy/createdOn/modifiedBy/modifiedOn`,每字段同时标 `@ExcelProperty`(导出表头)+ `@Schema` — **VO 天然可作 Excel 导出模型**

### BaseService 方法集(`framework.crud.service`)

```
String create(BDTO)
List<T> batchCreate(List<BDTO>)
void update(BDTO)  /  void batchUpdate(List<BDTO>)
void delete(String id)  /  void deletes(Set<String> ids)
T getById(String id)  /  List<T> getByIds(Set<String> ids)
long getCount(QDTO)
T getOne(QDTO)
List<T> search(QDTO)
Page<T> search(QDTO, int pageNum, int pageSize)
<V> Set<V> searchColumn(QDTO, Func1<T, V> column)
<DTO> List<DTO> searchJoinList(Class<DTO> clazz, MPJLambdaWrapper<?> wrapper)
```

### MybatisPlusServiceImpl(默认实现)

`extends MPJBaseServiceImpl<M, T, BDTO, QDTO>`,泛型 `<M extends BaseDao<T>, T extends BaseEntity, BDTO extends BaseDTO, QDTO extends QueryDTO>`;构造器用 Hutool `TypeUtil.getTypeArguments` 反射拿 DTO/QueryDTO 的 Class。

行为要点(生成等价代码时必须保留):

- `create`:DTO→Entity(`BeanUtil.toBean`)→ `initCreateInfo`(审计字段 = `SecurityContextHolder.getCurrentUsername()` / now,version=0)→ `beforeCreate(entity)` → save → `afterCreate(entity)` → 返回 id
- `update`:id 空抛 `BusinessException("ID为空,请检查")`;先 `getById` 取库内旧值 → `BeanUtil.copyProperties(dto, entity, CopyOptions 忽略null)` **部分更新语义**(未传字段保持原值,内存状态与落库一致;字段显式置空需专用端点) → `initUpdateInfo` → `beforeUpdate` → `updateById`(@Version 生效)→ `afterUpdate`
- `batchCreate` / `batchUpdate`:批量版,各自 `beforeCreate(List<T>)` / `afterCreate(List<T>)` 等重载钩子;batchUpdate 先按 id 集合查旧值再逐条覆盖
- 删除带 `beforeDelete/afterDelete` 钩子;查询:`queryCondition` 动态拼 where + `sortName/sortType` 排序
- **钩子命名固定**:`beforeCreate/afterCreate/beforeUpdate/afterUpdate/beforeDelete/afterDelete`(单、批重载)— 业务差异只允许覆写钩子,不许改写主流程
- **条件扣减模板** `conditionalDecrease(Func1<T,?> column, String id, Number delta)`:原子 SQL `SET 数量列=数量列-? WHERE id=? AND 数量列>=?`,数据库行锁防超卖;返回 false = 数据不存在或余量不足。库存/额度类扣减一律用它,禁止"先查后扣"两段式;列名经实体字段反解、delta 为服务端数值,无注入面

### RedisCacheServiceImpl(缓存实现,代码生成器的默认父类)

继承 MybatisPlusServiceImpl:

- **仅缓存 id 维度**(getById/getByIds 读穿:Redis hash,key 前缀 = `BaseDataCacheKey` 前缀 + 实体名,8h TTL,未命中回源回填);search/getOne/分页**不缓存**——查询组合无限膨胀、任意行变更需全量失效,直查数据库靠索引
- create/batchCreate/update/batchUpdate/delete/deletes:执行父类逻辑后按 id **删缓存**——删除在**事务提交后**执行(避免提交前删除被并发回填旧值);hash 写入统一按 KeyPrefix TTL 续期;删除子 key 用 SCAN(禁用 KEYS);查不到的数据不回写缓存
- 留 `fullDto(BDTO)` / `fullDto(List<BDTO>)` 空钩子给子类落库前补数据

### QueryBuilder(`framework.crud`)

服务端内部查询的链式构建器,产出 QueryDTO:

```
QueryBuilder.builder(XxxQueryDTO.class)
    .eq(Entity::getField, v) / ne / gt / ge / lt / le
    .between(Entity::getField, v1, v2)
    .in(Entity::getField, collection)     // value 逗号拼接
    .like(Entity::getField, v)
    .orderByASC(Entity::getField) / orderByDESC(...)
    .build();
```

column 参数一律 lambda(`Func1`),经 Hutool `LambdaUtil` 解析列名。前端筛选走 queryCondition JSON;后端逻辑查询走 QueryBuilder。

### CrudController(`framework.crud.web`)

泛型 `<BS extends BaseService<...>, BDTO extends BaseDTO, QDTO extends QueryDTO, VO extends BaseVO> extends BaseController`;构造器反射解析 service/DTO/VO 三个 Class(service 用 `SpringUtil.getBean` 取)。

| 端点 | 方法 | 入参 | 返回 |
|---|---|---|---|
| `POST /` | 创建 | `@Validated(CreateGroup) BDTO` | `String` id(produces text/plain) |
| `PUT /` | 更新 | `@Validated(UpdateGroup) BDTO` | void |
| `DELETE /` | 删除 | `BaseIdsDTO` | void |
| `POST /id` | 主键查 | `BaseIdDTO` | VO |
| `POST /ids` | 主键集合查 | `BaseIdsDTO` | `List<VO>` |
| `POST /one` | 条件查单个 | QDTO | VO |
| `POST /search` | 条件查集合 | `@Validated QDTO` | `List<VO>` |
| `POST /search/{pageNum}/{pageSize}` | 分页 | QDTO + 路径参数 | `Page<VO>` |

- Entity→VO 转换统一 `BeanUtil.toBean` / `copyToList`
- 钩子:`beforeSearch(QDTO)`(查询前加工条件)、`fillVo(VO)` / `fillVo(List<VO>)`(补关联数据、字典翻译)
- URL 前缀由子类 `@RequestMapping("/{模块名}/{entityPath}")` 决定
- 每端点带 `@Operation(summary=...)`

---

## framework/mastersub(主子表 / 单据)

凡"单据类"业务(订单、出入库单、申请单)一律走这套:

- `MasterBaseEntity`:BaseEntity + `order_num`(单据编号)+ `order_status`(单据状态)
- `SubBaseEntity`:BaseEntity + `master_id`(主表ID)
- `OrderBaseEntity`:BaseEntity + orderNum + orderStatus(单据语义更重的场景用)
- 配套 `MasterBaseService` / `SubBaseService` / `OrderBaseService` 及实现:主表 create/update 级联保存子表(整单先删后插或 diff)、按 masterId 查子表、单据号生成钩子、单据状态流转
- `MasterExcelController` / `SubExcelController` / `OrderExcelController`:主子表整体导入导出三件套

## framework/excel(fastexcel 导入导出)

`ExcelController extends CrudController`,**多一个泛型参数** `<BS, BDTO, EQDTO, VO, UploadDTO>`:

| 端点 | 行为 |
|---|---|
| `POST /excel/download` | 按 `ExcelQueryDTO.ExcelCondition { fileName; includeColumns; excludeColumns }` 导出 search 结果,文件名 `{名}_导出列表_{时间戳}.xlsx`,URLEncoder 处理中文 |
| `POST /excel/template` | 空数据导出 UploadDTO 表头 = 导入模板;空 sheet 启用内存模式 + `CommentWriteHandler` 写字段批注 |
| `POST /excel/upload` | MultipartFile 入参;`FastExcel.read` + `DefaultUploadDataListener` 逐行 jakarta 校验,错误收集 `List<ErrorMessage>`,不通过抛 `ExcelException("Excel基本数据校验失败！", errors)`;通过后交 `ExcelUploadService.handel(list)` 落库(子类可覆写 `getExcelUploadServiceClass` 换实现) |

- `ExcelQueryDTO extends QueryDTO` + `excelCondition` 字段
- UploadDTO 字段 fastexcel 注解 + jakarta 校验注解双标
- **行数上限保护**:默认 2 万行(`goldlion.excel.max-rows` 可调,子类可覆写 `getMaxRows`),超限抛 ExcelException 终止导入;读取结果直接使用,不做整表二次拷贝
- 监听器体系:`ListAnalysisEventListener`(基类,持 list + errors)→ `DefaultUploadDataListener`

## framework/security(OAuth2)

spring-boot-starter-oauth2-authorization-server:

- JDBC 三件套:`BaseJdbcRegisteredClientRepository` / `BaseJdbcOAuth2AuthorizationService` / `BaseJdbcOAuth2AuthorizationConsentService`(client 与授权数据落 MySQL,表结构随 Spring Authorization Server schema)
- `OAuth2SecurityConfig`:默认安全链;`OAuth2TokenFilter` 解析 token(**优先 Authorization Bearer 头**,不进访问日志;`access_token` 参数兼容存量前端)→ 构造 `AuthSecurityUser` → 放入 `SecurityContextHolder`;`BaseAuthorizationManager` 接口级鉴权
- `BaseOAuth2LoginServiceImpl` 登录;`LoginDataCacheKey`(Redis 缓存登录态);`PasswordUtils` 密码加密;`OAuth2Client` / `OAuth2User` 模型与客户端管理 Service
- 微服务形态:认证服务独立部署;资源服务引 token 校验 + TokenFilter

## framework/deadletter(MQ 死信)

- `DeadLetterLog` 模型(routingKey / scene / message / createdOn)+ Service + Controller(查询、手动重投)
- `DeadLetterMessageListener`:监听 `DEAD_LETTER_EXCHANGE`,**MANUAL ack**;解析 `xDeathHeader` → 落库 → `basicAck`;解析失败或异常 → **丢弃**(`basicReject(tag, false)`)并将完整消息体打 error 日志供人工重发(重新入队不会累计 x-death,会无限循环);channel 由监听容器托管,禁止手动 close
- 重投走 `RabbitProducer.reDelivery(DeadLetterLog)`

## framework/generator(代码生成器)

- `BaseGeneratorImpl`:MyBatis-Plus `FastAutoGenerator` + `FreemarkerTemplateEngine`;控制台 scanner 输入作者/表名;全局配置、包配置、策略配置(实体/Dao/Service/ServiceImpl/Controller 父类)、注入配置(DTO/QueryDTO/UploadDTO/VO 自定义模板)
- 三种实现:`SingleTableGeneratorImpl`(单表)/ `MasterSubTableGeneratorImpl`(主子表)/ `OrderTableGeneratorImpl`(单据);默认父类:Entity=BaseEntity、ServiceImpl=RedisCacheServiceImpl、Controller=ExcelController、DTO/QueryDTO/UploadDTO/VO 各自基类(全部可覆写)
- Freemarker 模板放 core 的 `resources/templates/`:Controller/Service/ServiceImpl/DTO/QueryDTO/UploadDTO/VO 的 .java.ftl + Insert.sql.ftl + 前端 Index.vue.ftl / Edit.vue.ftl / ApiService.js.ftl
- 新项目直接照搬模板目录;生成产物清单见 conventions.md

---

## middleware/redis

- `KeyPrefix` 接口 `{ long expireSeconds(); String getPrefix(); }` + `BaseKeyPrefix` 基类;**每个业务缓存定义枚举实现 KeyPrefix**(前缀含应用名、冒号分层),禁止裸字符串拼 key
- `RedisOperator`(@Component):value 族(`getValue/setValue[自动按 KeyPrefix TTL]/deleteValue/deleteAll/deleteAllChildren`)+ hash 族(`putHashValue/getHashValue/putAllHashValue/multiGetHashValue/valuesHashValue/deleteHashValue`)
- `JsonRedisTemplate`:JSON 序列化的 RedisTemplate(禁止 JDK 序列化);`RedisConfig` 注册

## middleware/mq

- `MQUsageScene` 接口 `{ getScene(); getQueue(); getRoutingKey(); }`;queue 命名 = `spring.application.name` + `.queue.` + 场景码(接口内 ThreadLocal PREFIX 读应用名)。**每个业务场景定义枚举实现该接口**(参考 `MQUsageSceneEnum`),scene 存入消息 header 便于死信溯源
- `MQExchangeEnum`:`DIRECT`(业务交换机)+ `DEAD_LETTER_EXCHANGE`
- `MQProducer` / `RabbitProducer`:`sendMsg(scene, msg)` — JSON body(UTF-8)+ header `scene` + **PERSISTENT 持久化** + `CorrelationData`(挂 publisher-confirm 回报:异常/nack 打 error 日志,需应用开启 confirm);业务消息模型继承 `BaseMQMessage` / `MQMessage`(自带 id/createdOn)
- 消费端:`@RabbitListener(bindings = @QueueBinding(...))` 声明式建队列/交换机,`ackMode = "MANUAL"`,业务失败 `basicReject(tag, true)`;消费侧审计身份用 `SecurityContextHolder` 的 `operateMqUserName`
- 失败消息最终进死信队列落库、人工重投;不做无限自动重试

## middleware/oss

`OssClient` 接口 + `OssClientAliYun` 实现(`OssClientSingleton` 单例,endpoint/accessKey/secretKey/bucket 走配置),`OssController` 提供上传/下载/删除端点,URL 缓存用 `OssDataCacheKey`。

---

## business(业务模板抽象)

- **order / `OrderOperateServiceImpl`**:`createOrder` 模板方法(事务内),四步抽象方法:
  1. `verifyOrderData(orderOperate)` → 校验失败返回 `Result.error`(注释明确:**校验热路径抛异常非常影响性能**,所以返回 Result 而非抛)
  2. `decreaseOrderInventory(orderOperate)` → false 抛 `BusinessException("库存不足")`
  3. `saveOrUpdateData(orderOperate)` → 异常时走 `handleSaveOrUpdateExceptions`(默认空实现,补偿/记录)后原样抛出
  4. 全过返回 `Result.success()`
  泛型 `<PK, OP extends BaseOrderOperate<? extends BaseOrderInfo<PK>, ? extends BaseOrderItem<PK>>>`;模型 `BaseOrderInfo` / `BaseOrderItem` / `BaseOrderOperate`
- **inventory/**:库存账户 `BaseInventoryAccount`(+ UniqueKey)、操作 `BaseInventoryOperate`、操作指令 `BaseInventoryOperateInstruction` + 对应 Service,`InventoryOperateDataCacheKey` 走 Redis;扣减库存走指令模式
- 新项目有订单/库存语义,直接继承这两套抽象,不自起炉灶

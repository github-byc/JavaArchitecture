# 编码约定

## 命名

| 对象 | 命名 |
|---|---|
| 类 | `XxxController` / `XxxService` / `XxxServiceImpl` / `XxxDao` / `Xxx`(实体)/ `XxxDTO` / `XxxQueryDTO` / `XxxUploadDTO` / `XxxVO` |
| 缓存 key | `XxxDataCacheKey`(枚举,实现 KeyPrefix) |
| MQ 场景 | `XxxSceneEnum`(枚举,实现 MQUsageScene) |
| 包 | 全小写;业务模块内按 `web / service / service.impl / dao / entity / dto / vo` 分包(参考生成模板) |
| 表/列 | snake_case;审计列固定 `created_by / created_on / modified_by / modified_on / version` |
| 审计语义 | 用 `_by`/`_on` 后缀(创建者/创建时间),不用 create_time 风格 |

生成模板里的包定位(Controller.ftl):Controller 在 `web`、ServiceImpl 在 `service.impl`、Dao 在 `dao`、DTO/QueryDTO/UploadDTO 在 `dto`(UploadDTO 子包 `dto.excel`)、VO 在 `vo`、Entity 在 `entity`。

## 分层与依赖

- 单向:`web → service → dao`;`Entity 不出 service 层`,`VO` 只出 controller,`DTO` 进出 service。
- 对象转换统一 Hutool `BeanUtil.toBean` / `copyProperties` / `copyToList`;不引 MapStruct。
- Controller 保持空壳(继承基类即可),只有动作型端点才写自定义方法;业务逻辑全在 ServiceImpl。

## REST 与返回

- 基类端点固定(见 base-classes.md CrudController 表),**POST 风格查询**,不自创 REST 风格。
- `create` 返回 `String id`(produces text/plain);增删改返回 void(前端以 code=200 判成功);查询返回 VO / `List<VO>` / `Page<VO>`;错误由全局异常处理兜底,正常路径不包 Result。
- 动作型端点:`POST /{动作}`(如提交/审核/作废),入参 BaseIdDTO 或专用 DTO。
- URL 前缀:`/{模块名}/{实体entityPath}`;@RequestMapping + @Tag 一起标。

## 校验与文档

- 入参 `@Validated` + 分组:创建 `CreateGroup.class`、更新 `UpdateGroup.class`;校验消息一律中文。
- Excel UploadDTO 字段同时标 fastexcel 注解(`@ExcelProperty`)+ jakarta 校验(`@NotNull` 等)。
- 文档全覆盖:Controller 类 `@Tag(name=...)`、端点 `@Operation(summary=...)`、所有 DTO/VO/Entity 字段 `@Schema(description=...)`;文档地址 `/doc.html`(knife4j)。

## 事务与并发

- ServiceImpl 写方法标 `@Transactional`;主子表整单保存必须同事务。
- 乐观锁 `@Version` 全表启用(插件必配,见 tech-stack.md);并发冲突靠 version 重试或报错,不加分布式读锁。
- 跨资源分布式锁用 Redisson。
- "事务提交后再做"(发 MQ、清缓存、通知)用 `TransactionalEvent`。

## 异常与日志

- 业务失败:`throw new BusinessException("中文消息")`;携带数据给前端时用其 data。
- 校验热路径(循环/批量/高频):**返回 `Result.error` 不抛异常**(性能,参考 order 模板方法注释)。
- Controller 不 try-catch,统一 `GlobalExceptionHandler`。
- 日志:`@Slf4j`;入出参 info、异常 error 带堆栈;SQL 日志 dev 走 p6spy。

## Redis / MQ / OSS 使用

- 缓存 key 一律 KeyPrefix 枚举(前缀含应用名、冒号分层、声明 TTL);`RedisCacheServiceImpl` 只做 **id 维度**读穿写删(列表/分页查询不缓存,靠索引),自定义缓存走 `RedisOperator`。
- MQ:新场景先在场景枚举登记(queue 前缀 = 应用名);生产走 `MQProducer.sendMsg`(持久化消息,开启 publisher-confirm 后发送结果自动打日志);消费 MANUAL ack + 幂等;失败靠死信落库 + 人工重投,不做无限重试。
- 对象存储走 `OssClient`,不直接引 SDK。

## 慢查询与索引

- 每表默认:主键 `id` + 业务唯一键;**高频筛选/排序字段必须建索引**(建表 SQL 一并声明,禁止"先上线后补索引")
- 动态 queryCondition 支持任意字段组合 → 新增可筛选字段时同步评估索引与区分度
- p6spy 仅 dev;prod 依赖数据库慢日志(如 `long_query_time=1s`)或 APM
- 连表(MPJ)注意驱动表索引与结果集规模;禁止在循环里逐条查库(N+1),用 getByIds/searchJoinList 批量
- 库存/数量扣减一律用基类 `conditionalDecrease`(原子条件更新),禁止"先查后扣"两段式

## 每表生成产物清单(九件套 + 附加)

代码生成器不可用时按此手工补齐,一件不能少:

| # | 产物 | 父类/基类 |
|---|---|---|
| 1 | Entity | BaseEntity |
| 2 | Dao | BaseDao |
| 3 | Service 接口 | BaseService |
| 4 | ServiceImpl | RedisCacheServiceImpl |
| 5 | Controller | ExcelController(不需要 Excel 用 CrudController) |
| 6 | DTO | BaseDTO |
| 7 | QueryDTO | ExcelQueryDTO(不需要 Excel 用 QueryDTO) |
| 8 | UploadDTO(按需) | ExcelUploadDTO |
| 9 | VO | BaseVO |
| + | Insert.sql(建表 + 审计列 + version) | — |
| + | 前端 Index.vue / Edit.vue / ApiService.js(有前端配套时) | — |

## 新增一个业务功能的标准动作

1. 建表:snake_case + 审计五列(id,version 外加审计四字段)。
2. 出九件套:继承基类空壳起步(生成器或手写)。
3. 业务逻辑只写在 ServiceImpl 的 `beforeXxx/afterXxx` 钩子与自有方法里。
4. 补关联数据/字典翻译 → 覆写 `fillVo`;查询前加工 → `beforeSearch`;服务端内部查询 → `QueryBuilder`。
5. 单据类 → 换 mastersub 三件套;有 Excel 需求 → UploadDTO + excel 三端点。
6. 落库前补数据 → `fullDto`;需要缓存业务对象 → 定义 KeyPrefix 枚举。
7. 补齐 @Tag/@Operation/@Schema,确认 knife4j 可见、`mvn compile` 通过。

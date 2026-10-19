---
name: java-backend-architecture
description: 搭建新的 Java 后端项目/服务、设计后端架构、生成项目脚手架、新增业务模块,或评审后端代码是否符合团队规范时必用。沉淀 goldlion 架构规范:多模块 Maven + common/framework/middleware/business 四层分包 + 泛型基类 CRUD 框架 + 统一 Result/异常/校验/Redis/MQ/OSS/Excel。技术栈固定 Java 17、Spring Boot 3、Spring Cloud Alibaba(Nacos)、MyBatis-Plus、MySQL、Redis、RabbitMQ,不得引入或推荐其他技术栈。触发词:新项目、搭建项目、初始化项目、新建服务、后端架构、脚手架、按架构来。
---

# Java 后端架构规范(goldlion)

本 skill 沉淀的是一套**后端架构约定**(源自团队 goldlion 架构实践),**自包含、零外部依赖**:不依赖任何本地参考工程,复制本文件夹到任意机器即可使用。

新项目**不依赖 goldlion 构件**:按本规范,在新工程自己的 groupId/包名下生成**等价代码**。

> **skill 版本**:v2.1(2026-10-19)。v2.0 随工程四轮优化升级;v2.1 为可移植修订(消除本地工程路径依赖,部署模板内嵌)。以最新版为准。

详细规范按需查阅(同目录 references/):

- [references/tech-stack.md](references/tech-stack.md) — 技术栈版本矩阵、parent pom 蓝图、单体/微服务两种形态、配置与部署
- [references/base-classes.md](references/base-classes.md) — 基类蓝图:common/framework/middleware/business 全部基类契约(新项目 core 模块照此生成)
- [references/conventions.md](references/conventions.md) — 命名、REST、校验、事务、异常、Redis/MQ/OSS 使用约定、每表生成产物清单
- [references/deploy-templates.md](references/deploy-templates.md) — 部署模板:Dockerfile + Jenkinsfile(内嵌,直接复制)

本文档与 references 是该架构的**唯一权威来源**:基类方法签名、行为语义一律以本文为准,不要凭记忆编造或臆测。

## 硬性原则

1. **技术栈固定**(版本矩阵见 tech-stack.md):Java 17 + Spring Boot 3 + Spring Cloud Alibaba(Nacos)+ MyBatis-Plus(+ mybatis-plus-join)+ MySQL/Druid/p6spy + Redis/Redisson + RabbitMQ + knife4j + fastexcel + Hutool/Lombok + 阿里云 OSS。不得引入或推荐其他栈(如 JPA、MongoDB、Kafka 等)。
2. **四层分包固定**:core 模块内 `common`(通用基础)/ `framework`(可复用框架:crud、mastersub、excel、security、deadletter、generator)/ `middleware`(redis、mq、oss)/ `business`(订单、库存等业务模板抽象)。
3. **一切实体走泛型基类链**:Entity→Dao→Service→ServiceImpl→Controller,配套 DTO/QueryDTO/VO(/UploadDTO)。业务代码只写"继承 + 覆写钩子"(beforeXxx/afterXxx/fillVo/beforeSearch),禁止重写 CRUD 轮子。
4. **统一横切面**:返回 Result(若依格式 code/msg/data)、全局异常 GlobalExceptionHandler、BusinessException 表业务失败、CreateGroup/UpdateGroup 分组校验、@Schema/@Tag 文档注解全覆盖、审计四字段 + @Version 乐观锁。
5. **单据类业务**(订单/出入库/申请单)一律用 mastersub 框架;订单创建用 business/order 模板方法;扣库存用 inventory 指令抽象。
6. **部署固定**:Jenkins 流水线 + Docker 镜像 + Nacos 注册/配置。

## 搭建新项目流程

**Step 0 — 先问再搭**(以下信息不许猜,一次问清):

1. 项目名 / artifactId / groupId / 基础包名
2. 形态:微服务(Nacos 注册 + 配置,多服务模块)还是单体(仅配置)
3. 中间件取舍:Redis / RabbitMQ / OSS / OAuth2 / Excel 哪些需要(不需要的 middleware 包不生成)
4. 表是否已设计:有 SQL 先按表生成九件套;没有则先出建表 SQL(snake_case + 审计四字段 + version)给用户确认

**Step 1 — parent pom**:照 tech-stack.md 蓝图,换 groupId,版本矩阵原样保留。

**Step 2 — core 模块**:照 base-classes.md 蓝图生成 `common` / `framework` / `middleware`(按 Step 0 取舍)/ `business`(按需)。包根为 `${基础包名}.core`。core 不含 @SpringBootApplication,但**自带 Spring 自动装配**(AutoConfiguration.imports,业务引依赖即得插件/模板/异常处理)+ **单测基线**(基类行为有回归用例,见 7.4 类比)。

**Step 3 — 业务模块**:每个实体九件套(清单见 conventions.md),Controller/ServiceImpl 空壳继承起步;微服务形态下每个服务一个可启动模块。

**Step 4 — 配置**:application.yml(datasource p6spy + Druid、redis、rabbitmq、knife4j、mybatis-plus 分页 + 乐观锁插件、nacos);dev profile 用 maven filtering 注入 Nacos 地址(机制见 tech-stack.md)。

**Step 5 — 部署文件**:按 [references/deploy-templates.md](references/deploy-templates.md)(内嵌模板)生成 Dockerfile + Jenkinsfile 到每个可启动模块,按注释替换占位符(仓库/命名空间/端口/部署动作)。

**Step 6 — 验证**:`mvn test` 全绿(单测是流水线门禁,不是可选项);启动类能起(或至少 Spring 上下文配置完整);knife4j 文档可见。

## 规范速查

| 项 | 约定 |
|---|---|
| 分层 | web → service → dao 单向依赖;Entity 不出 service 层,VO 出 controller,DTO 进出 service |
| REST | 基类端点固定(POST 风格):`POST /` 创建返回 String id、`PUT /` 更新、`DELETE /` 删除、`POST /id|/ids|/one|/search|/search/{pageNum}/{pageSize}` 查询;URL 前缀 `/{模块}/{实体}` |
| 查询 | 前端筛选传 QueryDTO.queryCondition(columnName + sqlKeyword + value);服务端内部查询用 QueryBuilder 链式 |
| 缓存 | key 一律 KeyPrefix 枚举(前缀含应用名);**仅 id 维度**读穿写删(列表/分页直查库靠索引);库存扣减用 `conditionalDecrease` 原子模板,禁止先查后扣 |
| MQ | 场景先登记枚举(scene/queue/routingKey,queue 前缀 = 应用名);生产走 MQProducer(消息持久化 + 发送回报);消费 MANUAL ack;失败进死信落库、人工重投 |
| 异常 | 业务失败 `throw new BusinessException(中文消息)`;热路径数据校验返回 Result.error(不抛异常);500 统一兜底文案不回传原始异常;controller 不 try-catch |
| 事务 | ServiceImpl 写方法 @Transactional;@Version 乐观锁 + 分页插件必配 |
| 文档 | 类 @Tag、字段 @Schema;文档地址 /doc.html |

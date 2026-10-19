# 技术栈与工程结构

## 版本矩阵(2026-10-19 升级后,源自 goldlion-parent)

| 类别 | 组件 | 版本 |
|---|---|---|
| 语言 | Java | 17 |
| 核心 | Spring Boot | **3.5.16**(3.x 终版;OSS 支持已停,商业支持至 2032) |
| 微服务 | Spring Cloud | **2025.0.3**(Northfields) |
| 微服务 | Spring Cloud Alibaba | **2025.0.0.0**(Nacos client 3.0.3;官方适配 Boot 3.5) |
| ORM | mybatis-plus-spring-boot3-starter | **3.5.11(上限)** |
| ORM | mybatis-plus-jsqlparser / mybatis-plus-generator | 3.5.11 |
| 连表 | mybatis-plus-join-boot-starter | **1.5.6**(MPJLambdaWrapper/MPJBaseMapper) |
| 代码生成 | freemarker | 2.3.34 |
| 数据库 | mysql-connector-j + druid-spring-boot-3-starter | Druid **1.2.28** |
| SQL 日志 | p6spy-spring-boot-starter | 1.11.0 |
| 缓存 | spring-boot-starter-data-redis | Boot 管理 |
| 分布式锁 | redisson-spring-boot-starter | **3.49.0** |
| MQ | spring-boot-starter-amqp(RabbitMQ) | Boot 管理 |
| 文档 | knife4j-openapi3-jakarta-spring-boot-starter | 4.5.0 + **springdoc 2.8.17 parent 层显式覆盖** |
| Excel | fastexcel(cn.idev.excel) | **1.2.0**(注意:项目正迁往 Apache Fesod,坐标暂不换) |
| 工具 | hutool-all(hutool-bom)+ lombok | Hutool **5.8.47** |
| 对象存储 | aliyun-sdk-oss(+ jaxb 三件) | 3.17.4 |
| 认证 | spring-boot-starter-oauth2-authorization-server | Boot 管理(SAS 1.5.8) |
| 校验/AOP/Web | spring-boot-starter-validation / aop / web | Boot 管理 |
| 微信(按需) | weixin-java-miniapp / cp / open | 4.7.0 |
| Servlet | jakarta.servlet-api | 版本交由 Boot BOM 管理(provided) |

**MyBatis-Plus 版本上限约束(重要)**:MP 3.5.12 起 `IService`/`ServiceImpl` 包名从 `extension.service` 改为 `spring.service`,mybatis-plus-join(最新 1.5.6)仍按旧包编译 → **MP 不可超过 3.5.11**,等 MPJ 适配后再单独升 MP。Boot 4.x 因 SCA/knife4j 生态未就绪,暂不升级。

规则:

- 版本号只出现在 parent 的 `<properties>` + `<dependencyManagement>`;业务模块声明依赖**不写版本号**。
- BOM 统一 import:spring-boot-dependencies、spring-cloud-dependencies、spring-cloud-alibaba-dependencies、mybatis-plus-bom、knife4j-dependencies、hutool-bom。
- 编码 UTF-8,编译目标 `${java.version}`。
- 曾有工程启用 compiler `<skip>true</skip>`(已移除):skip 会让 mvn 产物为空、CI 形同虚设;单测 `mvn test` 是流水线门禁,新项目照常构建。

## parent pom 蓝图(骨架)

```xml
<groupId>${YOUR_GROUP_ID}</groupId>
<artifactId>${YOUR_PROJECT}-parent</artifactId>
<version>1.0.0</version>
<packaging>pom</packaging>

<properties>
    <java.version>17</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <!-- 上表所有版本号 -->
</properties>

<dependencyManagement>
    <dependencies>
        <!-- BOM imports + 散件版本,内容与上方版本矩阵一一对应 -->
    </dependencies>
</dependencyManagement>

<profiles>
    <profile>
        <id>dev</id>
        <activation><activeByDefault>true</activeByDefault></activation>
        <properties>
            <profile.name>dev</profile.name>
            <config.username>${NACOS_USER}</config.username>
            <config.password>${NACOS_PWD}</config.password>
            <config.server-addr>${NACOS_ADDR}</config.server-addr>
            <config.namespace>${NAMESPACE}</config.namespace>
            <config.group>${GROUP}</config.group>
            <discovery.server-addr>${NACOS_ADDR}</discovery.server-addr>
        </properties>
    </profile>
    <!-- 测试/生产环境各加一个 profile,结构同上 -->
</profiles>
```

要点:**Nacos 地址/命名空间/账号一律走 maven profile + properties 注入**,配置文件里用 `${config.server-addr}` 等占位符引用,配合 `pom.xml` build 里 `maven-resources-plugins` 的 filtering。有私服再加 `<distributionManagement>`,没有就省略。

## 工程形态

### 微服务形态(多模块)

```
{project}/
  pom.xml                    # = parent(单根 pom 即可,或独立 {project}-parent)
  {project}-core/            # 框架模块,jar,见 base-classes.md
  {project}-{svc-a}/         # 业务服务 A,可启动
  {project}-{svc-b}/         # 业务服务 B,可启动
```

- 服务模块依赖 core;服务间调用 OpenFeign + Nacos 发现。
- 认证服务独立部署(authorization server),资源服务做 token 校验。
- 每个服务模块 build 带 `spring-boot-maven-plugin`(repackage 可执行 jar)。

### 单体形态

```
{project}/
  pom.xml                    # parent
  {project}-core/            # 框架模块
  {project}-web/             # 唯一启动模块,业务代码按 business.<域> 分包
```

### 依赖方向

业务模块 → core → 框架依赖。core 里**没有** @SpringBootApplication,不含启动类。

## application.yml 要点(dev 环境)

```yaml
spring:
  application:
    name: ${服务名}
  profiles:
    active: @profile.name@          # maven filtering 注入
  config:
    import: nacos:${spring.application.name}.yaml   # 微服务形态;单体只用配置中心也走这个
  cloud:
    nacos:
      config:
        server-addr: ${config.server-addr}
        namespace: ${config.namespace}
        group: ${config.group}
        username: ${config.username}
        password: ${config.password}
      discovery:
        server-addr: ${discovery.server-addr}
  datasource:
    driver-class-name: com.p6spy.engine.spy.P6SpyDriver     # p6spy 驱动
    url: jdbc:p6spy:mysql://${DB_HOST}:3306/${DB}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    type: com.alibaba.druid.pool.DruidDataSource
    druid: {初始连接数/最大连接数/监控统计按需}
  data:
    redis: {host/port/password/database}
  rabbitmq:
    {host/port/username/password}
    listener:
      simple:
        acknowledge-mode: manual    # 消费手动 ack
knife4j:
  enable: true
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```

**MyBatis-Plus 插件已由 core 自动装配**(2026-10-19 起):core 内 `MybatisPlusPluginsConfig` 注册分页 + 乐观锁拦截器,`GoldlionCoreAutoConfiguration`(AutoConfiguration.imports)统一装配 JsonRedisTemplate、RedisOperator、MQ 生产者、死信监听、全局异常处理、TraceId 拦截器。业务服务引依赖即得,无需再配;整体关闭:`goldlion.core.enabled=false`。

dev 环境配 p6spy 的 `spy.properties`(date 格式、SQL 打印);prod profile 关 SQL 日志。

## 部署(固定偏好)

- Jenkins 流水线构建(mvn package + docker build/push)+ Docker 镜像 + Nacos 注册。
- **标准模板**:见 [deploy-templates.md](deploy-templates.md)(内嵌 Dockerfile + Jenkinsfile),新服务复制到模块根目录,按注释替换占位符(仓库/命名空间/端口/部署动作)。
- Dockerfile 基线:`eclipse-temurin:17-jre`,拷贝可执行 jar,JVM 参数外置(`JAVA_OPTS`),时区 Asia/Shanghai;国内网络可把 BASE_IMAGE 指向内部镜像源。
- 配置中心管 dev/test/prod 差异;镜像里只留 bootstrap 级最小配置。
- 单元测试是流水线门禁:测试失败即构建失败,不允许 `-DskipTests` 通过。

# JavaArchitecture(goldlion 后端架构)

企业级 Java 后端脚手架:统一依赖管理的 parent + 泛型基类 CRUD 框架 core,配套编码规范 skill 与部署模板。

## 组成

| 目录 | 说明 |
|---|---|
| `goldlion-parent/` | Maven parent,统一依赖版本管理(Boot 3.5.16 / SC 2025.0.3 / SCA 2025.0.0.0 / MyBatis-Plus 3.5.11 等) |
| `goldlion-core/` | 框架核心 jar:common(通用基础)/ framework(crud、mastersub、excel、security、deadletter、generator)/ middleware(redis、mq、oss)/ business(订单、库存模板抽象);自带 Spring 自动装配 |
| `deploy/` | Dockerfile + Jenkinsfile 标准部署模板 |
| `.claude/skills/java-backend-architecture/` | 架构规范 skill(自包含,可复制到任意机器指导新项目搭建) |
| `优化记录.md` | 工程演进记录(四轮优化:正确性修复 → 重构 → 版本升级 → 测试/部署体系) |
| `skill-升级对比-v1-v2.md` | 架构规范 skill 的版本演进对比 |

## 快速开始

```bash
# 构建验证(含单元测试门禁)
cd goldlion-parent && mvn -N install
cd ../goldlion-core && mvn test
```

## 技术栈

Java 17 · Spring Boot 3.5.16 · Spring Cloud 2025.0.3 · Spring Cloud Alibaba 2025.0.0.0(Nacos)· MyBatis-Plus 3.5.11(+ mybatis-plus-join)· MySQL/Druid/p6spy · Redis/Redisson · RabbitMQ(死信落库重投)· knife4j + springdoc · fastexcel · Jenkins + Docker

> 注意:MyBatis-Plus 上限 3.5.11(MP 3.5.12+ 改包名,mybatis-plus-join 未适配);Boot 4.x 待 SCA/knife4j 生态就绪后再评估。

## 新项目脚手架

把 `.claude/skills/java-backend-architecture/` 复制到 `~/.claude/skills/`,对 Claude Code 说"搭建一个新的后端服务"即可按本架构生成新工程(详见 skill 内 SKILL.md)。

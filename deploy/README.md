# 部署模板使用说明

标准 Jenkins + Docker 部署模板,新业务服务**复制即用**,按注释替换占位符。

## 文件

| 文件 | 用途 |
|---|---|
| `Dockerfile` | 服务镜像模板(temurin-17-jre + 时区 + JAVA_OPTS 外置) |
| `Jenkinsfile` | 流水线模板(检出→打包→单测→镜像→推送→部署) |

## 使用步骤

1. 把 `Dockerfile`、`Jenkinsfile` 复制到**业务服务模块根目录**(与该模块 pom 同级)。
2. `Dockerfile`:改 `EXPOSE` 端口;按需启用 HEALTHCHECK(actuator 或 TCP)。
3. `Jenkinsfile`:
   - 替换 `REGISTRY` / `NAMESPACE` / `SERVICE_NAME`;
   - `harbor-credentials` 指向 Jenkins 凭据库里的镜像仓库账号;
   - `部署` stage 接入实际动作(SSH 拉起 / K8s / 部署平台)。
4. Jenkins 新建 Pipeline 任务 → 指向仓库与本文件。
5. Nacos 地址等环境差异走 Nacos 配置中心,镜像内只留启动必需参数。

## 约定

- 镜像 tag:非手动指定时为 `分支-构建号`,可回溯。
- 单测失败即流水线失败(门禁),禁止 `-DskipTests` 侥幸通过。
- JAVA_OPTS 通过 `docker run -e` 覆盖,调内存不需要重新构建镜像。
- 国内网络拉基础镜像可把 `BASE_IMAGE` 指向内部镜像源(如 docker.1ms.run/eclipse-temurin:17-jre)。

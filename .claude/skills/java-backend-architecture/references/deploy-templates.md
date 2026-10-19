# 部署模板(内嵌,Dockerfile + Jenkinsfile)

新服务搭建 Step 5 直接从本文档取模板,复制到**业务服务模块根目录**(与该模块 pom 同级),按注释替换占位符。本文件自包含,不依赖任何本地工程。

## Dockerfile

```dockerfile
# 标准服务镜像模板
# 构建:docker build -t {registry}/{namespace}/{service}:{tag} --build-arg JAR_FILE=target/xxx.jar .
# 私有仓库/镜像加速:可把 BASE_IMAGE 指向内部镜像源,如 docker.1ms.run/eclipse-temurin:17-jre
ARG BASE_IMAGE=eclipse-temurin:17-jre
FROM ${BASE_IMAGE}

# JAR 路径由构建参数传入,默认取 target 下唯一 jar
ARG JAR_FILE=target/*.jar
WORKDIR /app

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

COPY ${JAR_FILE} app.jar

# 健康检查按各服务实际端口/端点调整(示例为 actuator;未引 actuator 的服务用 TCP 检查或删掉)
# HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=60s \
#   CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# 服务端口按实际修改
EXPOSE 8080

# JAVA_OPTS 运行时可通过 -e 覆盖,无需重建镜像
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## Jenkinsfile

```groovy
// 标准流水线模板 —— 按注释替换占位符
pipeline {
    agent any

    parameters {
        string(name: 'BRANCH', defaultValue: 'develop', description: '构建分支')
        string(name: 'TAG', defaultValue: '', description: '镜像标签,默认 分支-构建号')
    }

    environment {
        // TODO 替换:镜像仓库地址 / 命名空间 / 服务名(与模块名一致)
        REGISTRY     = 'registry.example.com'
        NAMESPACE    = 'your-namespace'
        SERVICE_NAME = 'your-service'
        IMAGE        = "${REGISTRY}/${NAMESPACE}/${SERVICE_NAME}:${params.TAG ?: params.BRANCH + '-' + env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {
        stage('代码检出') {
            steps {
                checkout scm
            }
        }

        stage('编译打包') {
            steps {
                // 单测独立 stage,失败时上下文更清晰;打包跳过重复执行
                sh 'mvn -B -U clean package -DskipTests'
            }
        }

        stage('单元测试') {
            steps {
                sh 'mvn -B surefire:test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('构建镜像') {
            steps {
                // Dockerfile 与本文件同目录;JAR 路径按模块产物名修改
                sh 'docker build -t ${IMAGE} --build-arg JAR_FILE=target/*.jar .'
            }
        }

        stage('推送镜像') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'harbor-credentials',
                        usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login ${REGISTRY} -u "$DOCKER_USER" --password-stdin'
                    sh 'docker push ${IMAGE}'
                }
            }
        }

        stage('部署') {
            when {
                // 只有指定分支才自动部署,避免日常构建全部上线
                anyOf { branch 'develop'; branch 'master' }
            }
            steps {
                // TODO 按实际运行方式替换:SSH 到目标机 docker pull + restart / kubectl set image / 调用部署平台接口
                echo "镜像已就绪: ${IMAGE},请接入实际部署动作"
            }
        }
    }

    post {
        failure {
            echo '构建失败,请查看日志'
        }
        always {
            cleanWs()
        }
    }
}
```

## 使用约定

1. 复制 `Dockerfile`、`Jenkinsfile` 到业务服务模块根目录。
2. Dockerfile:改 `EXPOSE` 端口;按需启用 HEALTHCHECK。
3. Jenkinsfile:替换 `REGISTRY`/`NAMESPACE`/`SERVICE_NAME`;`harbor-credentials` 指向 Jenkins 凭据库;`部署` stage 接入实际动作(SSH 拉起 / K8s / 部署平台)。
4. 镜像 tag 默认 `分支-构建号`,可回溯;单测失败即流水线失败(门禁),禁止 `-DskipTests` 侥幸通过。
5. JAVA_OPTS 通过 `docker run -e` 覆盖,调内存不需要重建镜像。
6. Nacos 地址等环境差异走 Nacos 配置中心,镜像内只留启动必需参数。

# goldlion 标准服务镜像模板 —— 复制到各业务服务模块根目录使用
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

# ============
# haifeng-app 构建镜像（用户端，内存限制同 admin）
# ============

# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml ./
COPY haifeng-common/pom.xml haifeng-common/pom.xml
COPY haifeng-admin/pom.xml haifeng-admin/pom.xml
COPY haifeng-app/pom.xml haifeng-app/pom.xml
COPY haifeng-common/src haifeng-common/src
COPY haifeng-app/src haifeng-app/src

# 限制 Maven/JVM 内存，防止 2G 服务器 OOM
ENV MAVEN_OPTS="-Xmx768m -XX:MaxMetaspaceSize=128m" \
    JAVA_TOOL_OPTIONS="-Xmx768m"

RUN mvn -B clean package -pl haifeng-app -am -Dmaven.test.skip=true -q

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ENV TZ=Asia/Shanghai

# 运行时 JVM 限制
ENV JAVA_OPTS="-Xms128m -Xmx320m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

COPY --from=builder /build/haifeng-app/target/haifeng-app-*.jar app.jar

EXPOSE 18080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY gradlew build.gradle settings.gradle ./
COPY gradle/ ./gradle/
COPY src/main/ ./src/main/

# 兼容从 Windows 上传的脚本换行，由容器内的 JDK 编译并生成可运行 JAR。
RUN sed -i 's/\r$//' gradlew \
    && sh ./gradlew --no-daemon bootJar \
    && cp build/libs/*.jar /build/app.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /build/app.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

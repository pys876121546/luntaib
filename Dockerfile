FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/ ./libs/

RUN mv $(find /app/libs -name "*.jar" ! -name "*sources.jar" | head -1) /app/app.jar && rm -rf /app/libs

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

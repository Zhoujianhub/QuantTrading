# 直接运行阶段 - 使用预编译的 JAR
FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd -r quant && useradd -r -g quant quant

COPY quant-position/target/*.jar app.jar
COPY quant-position/src/main/resources/application-prod.yml /app/config/application.yml

RUN chown -R quant:quant /app

USER quant

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.config.location=/app/config/", "app.jar"]

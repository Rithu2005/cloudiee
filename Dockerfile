FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY target/*.jar app.jar

RUN apk add --no-cache docker-cli

CMD ["java", "-jar", "app.jar"]

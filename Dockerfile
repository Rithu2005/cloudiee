FROM eclipse-temurin:17-jdk-alpine

RUN apk add --no-cache \
    python3 \
    gcc \
    g++ \
    nodejs \
    npm

WORKDIR /app
COPY target/codeee-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

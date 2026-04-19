# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first for faster rebuilds.
COPY pom.xml .
RUN mvn -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -DskipTests clean package \
    && JAR_FILE=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*original*" | head -n 1) \
    && cp "$JAR_FILE" target/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

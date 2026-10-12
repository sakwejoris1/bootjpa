FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:11-jre-alpine

WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build --chown=spring:spring /workspace/target/bootjpa-0.0.1-SNAPSHOT.jar app.jar
USER spring

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

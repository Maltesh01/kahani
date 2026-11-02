FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY api/pom.xml api/pom.xml
RUN mvn -B -f api/pom.xml dependency:go-offline
COPY api/src api/src
RUN mvn -B -f api/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S kahani && adduser -S -G kahani kahani
WORKDIR /app
COPY --from=build --chown=kahani:kahani /workspace/api/target/kahani-api-0.0.1-SNAPSHOT.jar /app/app.jar
USER kahani
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]

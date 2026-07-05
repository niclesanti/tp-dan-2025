FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

COPY pom.xml ./
COPY common/dan-eureka-server/pom.xml ./common/dan-eureka-server/

RUN mvn dependency:go-offline -B -f common/dan-eureka-server/pom.xml || true

COPY common/dan-eureka-server/src ./common/dan-eureka-server/src

RUN mvn clean package -DskipTests -B -f common/dan-eureka-server/pom.xml

FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="tp-dan-2025"
LABEL service="dan-eureka-server"
LABEL version="1.0.0"

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder /workspace/common/dan-eureka-server/target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8761

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]

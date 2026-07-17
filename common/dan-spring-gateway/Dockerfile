# ╔════════════════════════════════════════════════════════╗
# ║ Multi-Stage Dockerfile para dan-spring-gateway        ║
# ║ Stage 1: Build con Maven (standalone)                 ║
# ║ Stage 2: Runtime con JRE Alpine (optimizado)          ║
# ║                                                        ║
# ║ NOTA: El contexto debe ser la raíz del proyecto       ║
# ╚════════════════════════════════════════════════════════╝

# ── STAGE 1: Build ────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copiar POMs
COPY pom.xml ./
COPY common/dan-spring-gateway/pom.xml ./common/dan-spring-gateway/
COPY common/dan-spring-gateway/mvnw ./common/dan-spring-gateway/
COPY common/dan-spring-gateway/.mvn ./common/dan-spring-gateway/.mvn/

# Convertir CRLF → LF y dar permisos de ejecución
RUN chmod +x common/dan-spring-gateway/mvnw && sed -i 's/\r$//' common/dan-spring-gateway/mvnw

# Descargar dependencias (se cachea si los POMs no cambian)
WORKDIR /workspace/common/dan-spring-gateway
RUN ./mvnw dependency:go-offline -B || true

# Copiar código fuente
COPY common/dan-spring-gateway/src ./src

# Compilar aplicación
RUN ./mvnw clean package -DskipTests -B

# ── STAGE 2: Runtime ──────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Metadata
LABEL maintainer="tp-dan-2025"
LABEL service="dan-spring-gateway"
LABEL version="1.0.0"

# Crear usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copiar JAR desde stage de build
COPY --from=builder /workspace/common/dan-spring-gateway/target/*.jar app.jar

# Cambiar ownership
RUN chown spring:spring app.jar

# Cambiar a usuario no-root
USER spring:spring

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Punto de entrada con configuración JVM optimizada
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]

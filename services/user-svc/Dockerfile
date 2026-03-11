# ╔════════════════════════════════════════════════════════╗
# ║ Multi-Stage Dockerfile para user-svc                  ║
# ║ Stage 1: Build con Maven                              ║
# ║ Stage 2: Runtime con JRE Alpine (optimizado)          ║
# ║                                                        ║
# ║ NOTA: El contexto debe ser la raíz del proyecto       ║
# ╚════════════════════════════════════════════════════════╝

# ── STAGE 1: Build ────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copiar POMs (padre e hijo) para cachear dependencias
COPY pom.xml ./
COPY services/user-svc/pom.xml ./services/user-svc/
COPY services/user-svc/mvnw ./services/user-svc/
COPY services/user-svc/.mvn ./services/user-svc/.mvn/

# Descargar dependencias (se cachea si los POMs no cambian)
WORKDIR /workspace/services/user-svc
RUN ./mvnw dependency:go-offline -B || true

# Copiar código fuente
COPY services/user-svc/src ./src

# Compilar aplicación
RUN ./mvnw clean package -DskipTests -B

# ── STAGE 2: Runtime ──────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Metadata
LABEL maintainer="tp-dan-2025"
LABEL service="user-svc"
LABEL version="1.0.0"

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copiar JAR desde stage de build
COPY --from=builder /workspace/services/user-svc/target/*.jar app.jar

# Cambiar ownership
RUN chown spring:spring app.jar

# Cambiar a usuario no-root
USER spring:spring

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Punto de entrada con configuración JVM optimizada
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]

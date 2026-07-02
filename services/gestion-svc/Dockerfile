# ╔════════════════════════════════════════════════════════╗
# ║ Multi-Stage Dockerfile para gestion-svc               ║
# ║ Stage 1: Build con Maven (multi-módulo -am)           ║
# ║ Stage 2: Runtime con JRE Alpine (optimizado)          ║
# ║                                                        ║
# ║ NOTA: El contexto debe ser la raíz del proyecto       ║
# ╚════════════════════════════════════════════════════════╝

# ── STAGE 1: Build ────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copiar mvnw y wrapper raíz (ejecuta el build multi-módulo)
COPY mvnw ./
COPY .mvn ./.mvn/
# Convertir CRLF → LF (necesario cuando el repo viene de Windows)
RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

# Copiar POMs para cachear dependencias antes de copiar fuentes
# Se copian TODOS los pom.xml del reactor para que Maven pueda resolver el árbol
# de módulos sin errores, aunque solo se compilen gestion-svc y dan-common-lib
COPY pom.xml ./
COPY common/dan-common-lib/pom.xml ./common/dan-common-lib/
COPY services/gestion-svc/pom.xml ./services/gestion-svc/
COPY services/user-svc/pom.xml ./services/user-svc/
COPY services/reservas-svc/pom.xml ./services/reservas-svc/
COPY common/dan-spring-gateway/pom.xml ./common/dan-spring-gateway/

# Descargar dependencias del árbol gestion-svc + dan-common-lib
RUN ./mvnw dependency:go-offline -pl services/gestion-svc -am -B || true

# Copiar código fuente
COPY common/dan-common-lib/src ./common/dan-common-lib/src
COPY services/gestion-svc/src ./services/gestion-svc/src

# Compilar aplicación (también construye dan-common-lib con -am)
RUN ./mvnw clean package -DskipTests -B -pl services/gestion-svc -am

# ── STAGE 2: Runtime ──────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Metadata
LABEL maintainer="tp-dan-2025"
LABEL service="gestion-svc"
LABEL version="1.0.0"

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copiar JAR desde stage de build
COPY --from=builder /workspace/services/gestion-svc/target/*.jar app.jar

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

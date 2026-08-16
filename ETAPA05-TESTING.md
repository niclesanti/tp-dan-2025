# ETAPA 05 - Guía de Verificación de Observabilidad

Comandos básicos para probar que el stack de observabilidad funciona de extremo a extremo. Cada comando incluye la **salida esperada** para confirmar que todo está OK.

> Precondición: stack levantado con `docker compose up -d --build` y todos los contenedores `Healthy`.

## 1. Estado del Stack

```powershell
docker compose ps
```

**Salida esperada:** todos los servicios con estado `Up` y columna `STATUS` = `healthy` (al menos los 5 microservicios + `otel-collector`, `prometheus`, `tempo`, `loki`, `promtail`, `grafana`).

## 2. Verificación Rápida de Backends (readiness)

```powershell
(Invoke-WebRequest -Uri 'http://localhost:9090/-/ready' -UseBasicParsing).StatusCode   # Prometheus
(Invoke-WebRequest -Uri 'http://localhost:3200/ready' -UseBasicParsing).StatusCode     # Tempo
(Invoke-WebRequest -Uri 'http://localhost:3100/ready' -UseBasicParsing).StatusCode     # Loki
(Invoke-WebRequest -Uri 'http://localhost:3000/api/health' -UseBasicParsing).StatusCode # Grafana
```

**Salida esperada:** `200` en los cuatro. (Grafana responde `200` a `/api/health` aunque no esté logueado; Tempo/Loki responden `200 ready`.)

## 3. Prometheus - Métricas

### 3.1. Targets de scraping

```powershell
(Invoke-WebRequest -Uri 'http://localhost:9090/api/v1/targets' -UseBasicParsing).Content
```

**Salida esperada:** JSON con `7` targets activos, todos con `"health":"up"`:
- `dan-eureka-server` (`http://dan-eureka-server:8761/actuator/prometheus`)
- `dan-gateway` (`http://dan-gateway:8080/actuator/prometheus`)
- `gestion-svc`, `reservas-svc`, `user-svc`
- `otel-collector` (`http://otel-collector:8889/metrics`)
- `prometheus` (self)

### 3.2. Serie `up` (conteo de targets vivos)

```powershell
(Invoke-RestMethod -Uri 'http://localhost:9090/api/v1/query?query=up' -TimeoutSec 10).data.result | ForEach-Object { "$($_.metric.job) = $($_.value[1])" }
```

**Salida esperada:** 7 líneas, todas con `= 1` (1 = target scapea OK; 0 = caído).

### 3.3. Métricas JVM de los microservicios

```powershell
(Invoke-RestMethod -Uri 'http://localhost:9090/api/v1/query?query=count(jvm_memory_used_bytes)' -TimeoutSec 10).data.result[0].value[1]
```

**Salida esperada:** número `> 0` (aprox. `80` series de memoria JVM en todo el stack).

### 3.4. Tasa de requests HTTP (confirma que hay tráfico real)

```powershell
(Invoke-RestMethod -Uri 'http://localhost:9090/api/v1/query?query=sum(rate(http_server_requests_seconds_count[5m]))' -TimeoutSec 10).data.result[0].value[1]
```

**Salida esperada:** número decimal `> 0` (ej. `0.97`). Si da `0`, generar tráfico (ver sección 7) y reintentar.

### 3.5. Métricas Prometheus de un microservicio vía PromQL

```powershell
# Ej: métricas de memoria JVM del user-svc (series individuales)
(Invoke-RestMethod -Uri 'http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes' -TimeoutSec 10).data.result | Where-Object { $_.metric.job -eq 'user-svc' } | ForEach-Object { "$($_.metric.area)/$($_.metric.id) = $($_.value[1])" }
```

**Salida esperada:** líneas `heap/nonheap` con valores en bytes (`> 0`). NOTA: los microservicios solo exponen el puerto 8080 interno (no mapeado a host); el scrapeo se hace vía red Docker, por eso se consulta Prometheus y no el endpoint directo del servicio.

## 4. Tempo - Trazas

### 4.1. Readiness

```powershell
(Invoke-WebRequest -Uri 'http://localhost:3200/ready' -UseBasicParsing).StatusCode
```

**Salida esperada:** `200` (body: `ready`).

### 4.2. Búsqueda de trazas (requiere header `X-Scope-OrgID`)

```powershell
(Invoke-RestMethod -Uri 'http://localhost:3200/api/search?limit=5' -Headers @{'X-Scope-OrgID'='anonymous'} -TimeoutSec 10).traces | ForEach-Object { "traceID=$($_.traceID) service=$($_.rootServiceName)" }
```

**Salida esperada:** al menos 1 línea con un `traceID` de 32 hex y un `service` (ej. `user-svc`, `reservas-svc`, `dan-eureka-server`). Un stack recién arrancado puede necesitar tráfico previo (sección 7).

### 4.3. Trazas por servicio (ej. gateway)

```powershell
(Invoke-RestMethod -Uri 'http://localhost:3200/api/search?limit=10&tags=service.name%3Ddan-spring-gateway' -Headers @{'X-Scope-OrgID'='anonymous'} -TimeoutSec 10).traces.Count
```

**Salida esperada:** número `> 0`. (El gateway se registra como `dan-spring-gateway`, no `dan-gateway`.)

### 4.4. Detalle de una traza (usar un `traceID` del paso 4.2)

```powershell
$tid = "<traceID del paso 4.2>"
(Invoke-RestMethod -Uri "http://localhost:3200/api/traces/$tid" -Headers @{'X-Scope-OrgID'='anonymous'} -TimeoutSec 10).batches[0].resourceSpans.Count
```

**Salida esperada:** número `>= 1` (spans de la traza). Si devuelve error, verificar que el `traceID` sea real y esté dentro de la retención.

## 5. Loki - Logs

### 5.1. Readiness

```powershell
(Invoke-WebRequest -Uri 'http://localhost:3100/ready' -UseBasicParsing).StatusCode
```

**Salida esperada:** `200`.

### 5.2. Logs OTLP de un servicio (logs de aplicación vía OpenTelemetry)

```powershell
$q = [uri]::EscapeDataString('{exporter="OTLP", service_name="user-svc"}')
(Invoke-RestMethod -Uri "http://localhost:3100/loki/api/v1/query_range?query=$q&limit=1" -Headers @{'X-Scope-OrgID'='anonymous'} -TimeoutSec 10).data.result | ForEach-Object { $_.stream | ConvertTo-Json -Compress }
```

**Salida esperada:** al menos 1 stream con labels `exporter="OTLP"`, `service_name="user-svc"`, `job="user-svc"`, `level`. Todos los servicios tienen logs OTLP:
- `user-svc`, `gestion-svc`, `reservas-svc`, `dan-eureka-server`, `dan-spring-gateway` (este último con `service_name="dan-spring-gateway"`).

### 5.3. Correlación log → trace (verificar que un log record lleva `traceid`)

```powershell
$q = [uri]::EscapeDataString('{exporter="OTLP", service_name="user-svc"}')
$r = (Invoke-RestMethod -Uri "http://localhost:3100/loki/api/v1/query_range?query=$q&limit=50" -Headers @{'X-Scope-OrgID'='anonymous'} -TimeoutSec 10).data.result
$r | ForEach-Object { $_.values } | ForEach-Object { $_[1] } | Where-Object { $_ -match '"traceid"' } | Select-Object -First 1
```

**Salida esperada:** un JSON de log record con campos `"traceid":"..."` y `"spanid":"..."`. El `traceid` debe coincidir con el `traceID` que Tempo devuelve para el mismo request (sección 4). Solo los logs emitidos dentro de un request (ej. consultas Hibernate, servicios) llevan trace context; logs de arranque no.

### 5.4. Logs con Promtail (logs de contenedores Docker)

```powershell
$q = [uri]::EscapeDataString('{job="containerlogs"}')
(Invoke-RestMethod -Uri "http://localhost:3100/loki/api/v1/query_range?query=$q&limit=1" -Headers @{'X-Scope-OrgID'='anonymous'} -TimeoutSec 10).data.result.Count
```

**Salida esperada:** número `> 0` (streams de logs de contenedores). Los streams de Promtail llevan labels `job="containerlogs"`, `service_name="containerlogs"`, `stream="stdout"|"stderr"` y `filename=...`. En el body JSON se ve el `"service":"<nombre>"` de cada microservicio.

## 6. Collector OTLP (OpenTelemetry Collector)

### 6.1. Endpoint OTLP HTTP (debe responder 200 a POST de señales)

```powershell
# Desde el host (puerto 4318 mapeado al collector). Un body vacío es un request protobuf válido.
curl.exe -s -o NUL -w "traces=%{http_code} " -X POST --data-binary "" -H "Content-Type: application/x-protobuf" http://localhost:4318/v1/traces
curl.exe -s -o NUL -w "metrics=%{http_code} " -X POST --data-binary "" -H "Content-Type: application/x-protobuf" http://localhost:4318/v1/metrics
curl.exe -s -o NUL -w "logs=%{http_code}" -X POST --data-binary "" -H "Content-Type: application/x-protobuf" http://localhost:4318/v1/logs
```

**Salida esperada:** `traces=200 metrics=200 logs=200`. Un `404` indica endpoint mal configurado: el path **debe** incluir `/v1/traces`, `/v1/metrics` o `/v1/logs`. Un `400` con body no vacío es normal (el payload `{}` no es protobuf válido).

### 6.2. Métricas del propio collector

```powershell
(Invoke-RestMethod -Uri 'http://localhost:9090/api/v1/query?query=up{job="otel-collector"}' -TimeoutSec 10).data.result[0].value[1]
```

**Salida esperada:** `1`.

## 7. Generar Tráfico para Poblar los Backends

```powershell
# Un request al gateway (genera span HTTP + logs correlacionados en los servicios enrutados)
(Invoke-WebRequest -Uri 'http://localhost:8080/users/bancos' -UseBasicParsing).StatusCode
(Invoke-WebRequest -Uri 'http://localhost:8080/reservas/reservas/huesped/dni/12345678' -UseBasicParsing).StatusCode
(Invoke-WebRequest -Uri 'http://localhost:8080/gestion/hoteles?page=0&size=10' -UseBasicParsing).StatusCode
Start-Sleep -Seconds 10
```

**Salida esperada:** `200` en cada request (el segundo puede devolver `404` si el DNI no existe; es válido, igualmente genera traza). Esperar ~10 s a que el collector procese en batch y luego repetir las consultas de las secciones 3, 4 y 5.

## 8. Grafana

1. Abrir `http://localhost:3000` (usuario/contraseña `admin`/`admin`).
2. **Connection → Data sources:** deben existir 3 datasources configurados y respondiendo:
   - `Prometheus` → *Save & test* da `Success`.
   - `Tempo` → *Save & test* da `Success`.
   - `Loki` → *Save & test* da `Success`.
3. **Dashboards → Spring Boot Microservices Dashboard:** los paneles muestran datos (HTTP rate, JVM, conexiones DB).
4. **Explore → Loki:** seleccionar `Loki` y filtrar `{service_name="user-svc"}` → deben aparecer líneas de log.

**Salida esperada:** los 3 datasources reportan conexión exitosa y el dashboard muestra series de datos no vacías.

## 9. Resumen de Puertos

| Servicio | Puerto | Uso |
|----------|--------|-----|
| Grafana | 3000 | UI (admin/admin) |
| Prometheus | 9090 | API de métricas |
| Tempo | 3200 | API de trazas |
| Loki | 3100 | API de logs |
| OTel Collector (OTLP HTTP) | 4318 | Recepción OTLP (traces/metrics/logs) |
| OTel Collector (OTLP gRPC) | 4317 | Recepción OTLP gRPC |
| Gateway | 8080 | Microservicios (único puerto de servicio expuesto) |

> Los microservicios (user-svc, gestion-svc, reservas-svc, eureka) no exponen puerto al host: se acceden solo a través del gateway (`:8080`) o por la red Docker interna.
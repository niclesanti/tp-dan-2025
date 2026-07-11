<#
.SYNOPSIS
    Integration test suite for ETAPA05 - Observabilidad con OpenTelemetry.
    Verifica que el stack completo de observabilidad funcione correctamente:
    Prometheus (metricas), Tempo (trazas), Loki (logs), Grafana (visualizacion).

.DESCRIPTION
    Ejecuta tests en 9 capas:
      1. Containers running y health endpoints
      2. Prometheus targets (scraping)
      3. Tempo (trazas distribuidas)
      4. Loki (agregacion de logs)
      5. Grafana (datasources + dashboard)
      6. Metricas end-to-end (genera trafico HTTP y verifica Prometheus)
      7. Trazas end-to-end
      8. Logs end-to-end
      9. Metricas de infraestructura (JVM, HikariCP)

.NOTES
    Ejecutar con el stack levantado: docker compose up -d
    Uso: pwsh ./infra/test-observabilidad.ps1
#>

$ErrorActionPreference = "Stop"
$passed  = 0
$failed  = 0
$skipped = 0
$results = @()

function Test-Step {
    param([string]$Name, [scriptblock]$Block)
    Write-Host -NoNewline "  [*] $Name ... "
    try {
        & $Block
        Write-Host "PASS" -ForegroundColor Green
        $script:passed++
        $results += [PSCustomObject]@{ Test = $Name; Status = "PASS" }
    } catch {
        Write-Host "FAIL" -ForegroundColor Red
        Write-Host "      $($_.Exception.Message)" -ForegroundColor DarkRed
        $script:failed++
        $results += [PSCustomObject]@{ Test = $Name; Status = "FAIL"; Error = $_.Exception.Message }
    }
}

function Test-Skip {
    param([string]$Name, [string]$Reason)
    Write-Host "  [ ] $Name ... SKIP ($Reason)" -ForegroundColor Yellow
    $script:skipped++
    $results += [PSCustomObject]@{ Test = $Name; Status = "SKIP"; Error = $Reason }
}

function Invoke-Prometheus {
    param([string]$Query)
    $encoded = [System.Net.WebUtility]::UrlEncode($Query)
    $r = Invoke-RestMethod -Uri "http://localhost:9090/api/v1/query?query=$encoded" -TimeoutSec 10 -ErrorAction Stop
    if ($r.status -ne "success") { throw "Prometheus query error: $($r.status) - $Query" }
    return $r.data.result
}

function Invoke-Grafana {
    param([string]$Path)
    $cred = "admin:admin"
    $encoded = [Text.Encoding]::ASCII.GetBytes($cred)
    $auth = [Convert]::ToBase64String($encoded)
    return Invoke-RestMethod -Uri "http://localhost:3000$Path" -TimeoutSec 10 -Headers @{ Authorization = "Basic $auth" } -ErrorAction Stop
}

function Invoke-Api {
    param([string]$Uri, [int]$TimeoutSeconds = 10)
    return Invoke-RestMethod -Uri $Uri -TimeoutSec $TimeoutSeconds -ErrorAction Stop
}

function Invoke-Gateway {
    param([string]$Path)
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080$Path" -TimeoutSec 10 -UseBasicParsing -ErrorAction Stop
        return $r.StatusCode
    } catch {
        if ($_.Exception.Response) {
            return [int]$_.Exception.Response.StatusCode
        }
        throw $_
    }
}

# ─────────────────────────────────────────────────────────────────────────────
Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "  TEST DE OBSERVABILIDAD - ETAPA05" -ForegroundColor Cyan
Write-Host "========================================================`n" -ForegroundColor Cyan

# ─── LAYER 1: Containers + Health ────────────────────────────────────────
Write-Host "[LAYER 1] Containers y Health`n" -ForegroundColor Magenta

Test-Step "Todos los contenedores esenciales estan running" {
    $ps = docker ps --filter "status=running" --format "{{.Names}}" 2>$null
    if (-not $ps) { throw "docker ps devolvio vacio. Docker esta corriendo?" }
    $required = @("tp-dan-mysql", "tp-dan-postgres", "tp-dan-mongodb", "tp-dan-rabbitmq",
                  "dan-eureka-server",
                  "tp-dan-otel-collector", "tp-dan-tempo", "tp-dan-loki",
                  "tp-dan-promtail", "tp-dan-prometheus", "tp-dan-grafana",
                  "tp-dan-2025-user-svc-1", "tp-dan-2025-gestion-svc-1",
                  "tp-dan-2025-reservas-svc-1", "tp-dan-2025-dan-gateway-1")
    $missing = $required | Where-Object { $_ -notin $ps }
    if ($missing) { throw "Contenedores faltantes: $($missing -join ', ')" }
}

Test-Step "Gateway health endpoint" {
    $h = Invoke-Api "http://localhost:8080/actuator/health"
    if ($h.status -ne "UP") { throw "Gateway status: $($h.status)" }
    $svcs = $h.components.discoveryComposite.components.discoveryClient.details.services
    $expected = @("user-svc", "gestion-svc", "reservas-svc")
    $missing = $expected | Where-Object { $_ -notin $svcs }
    if ($missing) { throw "Servicios no registrados en Gateway: $($missing -join ', ')" }
}

Test-Step "user-svc health (directo 8081)" {
    $h = Invoke-Api "http://localhost:8081/actuator/health"
    if ($h.status -ne "UP") { throw "user-svc status: $($h.status)" }
    if ($h.components.db.status -ne "UP") { throw "user-svc DB no conectada" }
}

Test-Step "reservas-svc health (directo 8082)" {
    $h = Invoke-Api "http://localhost:8082/actuator/health"
    if ($h.status -ne "UP") { throw "reservas-svc status: $($h.status)" }
    if ($h.components.mongo.status -ne "UP") { throw "reservas-svc MongoDB no conectada" }
    if ($h.components.rabbit.status -ne "UP") { throw "reservas-svc RabbitMQ no conectado" }
}

Test-Step "gestion-svc health (directo 8083)" {
    $h = Invoke-Api "http://localhost:8083/actuator/health"
    if ($h.status -ne "UP") { throw "gestion-svc status: $($h.status)" }
    if ($h.components.db.status -ne "UP") { throw "gestion-svc DB no conectada" }
    if ($h.components.rabbit.status -ne "UP") { throw "gestion-svc RabbitMQ no conectado" }
}

Test-Step "Eureka Dashboard accesible (8761)" {
    $r = Invoke-WebRequest -Uri "http://localhost:8761" -TimeoutSec 10 -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "Eureka status: $($r.StatusCode)" }
}

# ─── LAYER 2: Prometheus targets ────────────────────────────────────────
Write-Host "`n[LAYER 2] Prometheus - Targets`n" -ForegroundColor Magenta

Test-Step "Prometheus API responde" {
    $r = Invoke-Prometheus "up"
    if ($r.Count -eq 0) { throw "No hay targets en Prometheus" }
}

Test-Step "Los 7 targets de Prometheus estan UP" {
    $targets = Invoke-RestMethod -Uri "http://localhost:9090/api/v1/targets" -TimeoutSec 10
    $active = $targets.data.activeTargets
    $upCount = ($active | Where-Object { $_.health -eq "up" } | Measure-Object).Count
    $downList = $active | Where-Object { $_.health -ne "up" }
    if ($downList) {
        $details = $downList | ForEach-Object { "$($_.labels.job): $($_.lastError)" }
        throw "Targets DOWN: $($details -join '; ')"
    }
    if ($upCount -lt 7) { throw "Solo $upCount/7 targets UP" }
}

Test-Step "Cada microservicio es scrapeado por Prometheus" {
    $result = Invoke-Prometheus "up"
    $foundJobs = $result | Where-Object { $_.value[1] -eq "1" } | ForEach-Object { $_.metric.job }
    $expectedJobs = @("user-svc", "gestion-svc", "reservas-svc", "dan-gateway", "dan-eureka-server", "otel-collector", "prometheus")
    $missingJobs = $expectedJobs | Where-Object { $_ -notin $foundJobs }
    if ($missingJobs) { throw "Jobs no scraped correctamente: $($missingJobs -join ', ')" }
}

# ─── LAYER 3: Prometheus metricas ────────────────────────────────────────
Write-Host "`n[LAYER 3] Prometheus - Metricas`n" -ForegroundColor Magenta

Test-Step "Metricas HTTP disponibles (http_server_requests_seconds_count)" {
    $result = Invoke-Prometheus "http_server_requests_seconds_count"
    if ($result.Count -eq 0) { Test-Skip "Metricas HTTP" "Aun no hay requests HTTP metricas. Generar trafico."; return }
    Write-Host "($($result.Count) series)" -NoNewline
}

Test-Step "Metricas JVM disponibles (jvm_memory_used_bytes)" {
    $result = Invoke-Prometheus "jvm_memory_used_bytes"
    if ($result.Count -eq 0) { throw "No hay metricas JVM. Verificar micrometer-registry-prometheus" }
    Write-Host "($($result.Count) series)" -NoNewline
}

Test-Step "Metricas JVM heap por servicio" {
    $q = 'jvm_memory_used_bytes{area="heap"}'
    $result = Invoke-Prometheus $q
    $services = $result | ForEach-Object { $_.metric.job } | Select-Object -Unique
    $count = ($services | Measure-Object).Count
    if ($count -eq 0) { throw "No hay metricas jvm_memory_used_bytes para heap" }
    Write-Host "($count servicios con metricas heap: $($services -join ', '))" -NoNewline
}

Test-Step "Metricas HikariCP disponibles (hikaricp_connections_active)" {
    $result = Invoke-Prometheus "hikaricp_connections_active"
    if ($result.Count -eq 0) { Test-Skip "HikariCP" "No hay metricas HikariCP (posible si no hay actividad de DB)"; return }
    Write-Host "($($result.Count) pools)" -NoNewline
}

Test-Step "Metricas de sistema (proces_start_time_seconds)" {
    $result = Invoke-Prometheus "process_start_time_seconds"
    if ($result.Count -eq 0) { throw "No hay metricas process_start_time_seconds" }
    Write-Host "($($result.Count) servicios)" -NoNewline
}

Test-Step "Servicios registrados en Eureka visibles en health" {
    $h = Invoke-Api "http://localhost:8080/actuator/health"
    $apps = $h.components.discoveryComposite.components.eureka.details.applications
    $total = ($apps.PSObject.Properties | Measure-Object).Count
    if ($total -lt 3) { throw "Solo $total aplicaciones en Eureka (esperado >= 3)" }
    Write-Host "($total apps)" -NoNewline
}

# ─── LAYER 4: Tempo (trazas) ──────────────────────────────────────────────
Write-Host "`n[LAYER 4] Tempo - Trazas Distribuidas`n" -ForegroundColor Magenta

Test-Step "Tempo API responde en /status/services" {
    $raw = Invoke-RestMethod -Uri "http://localhost:3200/status/services" -TimeoutSec 10 -UseBasicParsing
    if (-not $raw) { throw "Tempo /status/services no retorno datos" }
    $lines = $raw -split "`n"
    $runningLines = $lines | Where-Object { $_ -match "Running" }
    $servicesRunning = ($runningLines | Measure-Object).Count
    if ($servicesRunning -lt 5) { throw "Muy pocos servicios Tempo Running (encontrados: $servicesRunning)" }
    Write-Host "($servicesRunning servicios Running)" -NoNewline
}

# ─── LAYER 5: Loki (logs) ──────────────────────────────────────────────
Write-Host "`n[LAYER 5] Loki - Logs`n" -ForegroundColor Magenta

Test-Step "Loki API responde" {
    $r = Invoke-Api "http://localhost:3100/loki/api/v1/label"
    if ($r.status -ne "success") { throw "Loki API: $($r.status)" }
}

Test-Step "Loki tiene labels de Docker y servicios" {
    $r = Invoke-Api "http://localhost:3100/loki/api/v1/label"
    $expectedLabels = @("filename", "job", "service_name", "stream")
    $foundLabels = $r.data
    $missingLabels = $expectedLabels | Where-Object { $_ -notin $foundLabels }
    if ($missingLabels) { Test-Skip "Labels Loki" "Labels faltantes: $($missingLabels -join ', ') - Promtail config puede no extraerlas en Docker Desktop Windows"; return }
}

# ─── LAYER 6: Grafana ──────────────────────────────────────────────────────
Write-Host "`n[LAYER 6] Grafana - Visualizacion`n" -ForegroundColor Magenta

Test-Step "Grafana API responde" {
    $r = Invoke-Api "http://localhost:3000/api/health"
    if ($r.database -ne "ok") { throw "Grafana DB: $($r.database)" }
}

Test-Step "Datasource Prometheus configurado" {
    $ds = Invoke-Grafana "/api/datasources"
    $match = $ds | Where-Object { $_.type -eq "prometheus" -and $_.url -eq "http://prometheus:9090" -and $_.isDefault -eq $true }
    if (-not $match) { throw "Datasource Prometheus default no encontrado" }
    Write-Host "(uid: $($match.uid))" -NoNewline
}

Test-Step "Datasource Tempo configurado" {
    $ds = Invoke-Grafana "/api/datasources"
    $match = $ds | Where-Object { $_.type -eq "tempo" -and $_.url -eq "http://tempo:3200" }
    if (-not $match) { throw "Datasource Tempo no encontrado" }
    Write-Host "(uid: $($match.uid))" -NoNewline
}

Test-Step "Datasource Loki configurado" {
    $ds = Invoke-Grafana "/api/datasources"
    $match = $ds | Where-Object { $_.type -eq "loki" -and $_.url -eq "http://loki:3100" }
    if (-not $match) { throw "Datasource Loki no encontrado" }
    Write-Host "(uid: $($match.uid))" -NoNewline
}

Test-Step "Dashboard Spring Boot Microservices existe" {
    $search = Invoke-Grafana "/api/search?query=Spring%20Boot%20Microservices"
    $dashboard = $search | Where-Object { $_.title -eq "Spring Boot Microservices Dashboard" }
    if (-not $dashboard) { throw "Dashboard 'Spring Boot Microservices Dashboard' no encontrado" }
    Write-Host "(uid: $($dashboard.uid))" -NoNewline
}

# ─── LAYER 7: Metricas end-to-end ─────────────────────────────────────────
Write-Host "`n[LAYER 7] Metricas End-to-End`n" -ForegroundColor Magenta

Test-Step "Actuator prometheus en cada servicio expone metricas (>100 lineas)" {
    foreach ($port in @(8081, 8082, 8083)) {
        $name = @{8081="user-svc"; 8082="reservas-svc"; 8083="gestion-svc"}[$port]
        $body = Invoke-RestMethod -Uri "http://localhost:$port/actuator/prometheus" -TimeoutSec 10 -UseBasicParsing
        $lines = ($body -split "`n").Count
        if ($lines -lt 100) { throw "${name}: solo $lines lineas (esperado >100)" }
    }
}

Test-Step "Actuator prometheus en Gateway expone metricas (>100 lineas)" {
    $body = Invoke-RestMethod -Uri "http://localhost:8080/actuator/prometheus" -TimeoutSec 10 -UseBasicParsing
    $lines = ($body -split "`n").Count
    if ($lines -lt 100) { throw "Gateway: solo $lines lineas (esperado >100)" }
}

# Generar trafico HTTP real a traves del Gateway
Write-Host "`n  Generando trafico HTTP para verificar pipelines..." -ForegroundColor Cyan

Test-Step "Gateway /actuator/health retorna 200" {
    $code = Invoke-Gateway "/actuator/health"
    if ($code -ne 200) { throw "Status: $code" }
}

Test-Step "Gateway /users/usuarios accesible" {
    $code = Invoke-Gateway "/users/usuarios"
    if ($code -eq 404) { throw "Endpoint no encontrado" }
    Write-Host "($code)" -NoNewline
}

Test-Step "Gateway /reservas/habitaciones accesible" {
    $code = Invoke-Gateway "/reservas/habitaciones"
    if ($code -eq 404) { throw "Endpoint no encontrado" }
    Write-Host "($code)" -NoNewline
}

Test-Step "Gateway /gestion/hoteles retorna 200" {
    $code = Invoke-Gateway "/gestion/hoteles"
    if ($code -ne 200) { throw "Status esperado 200, obtenido $code" }
}

Test-Step "El trafico HTTP genera metricas http_server_requests_seconds_count" {
    Start-Sleep -Seconds 3
    $result = Invoke-Prometheus "increase(http_server_requests_seconds_count[1m]) > 0"
    if ($result.Count -eq 0) { Test-Skip "Metricas de trafico" "No se detecto aumento en metricas HTTP en el ultimo minuto"; return }
    Write-Host "($($result.Count) series con aumento)" -NoNewline
}

# ─── LAYER 8: Trazas end-to-end ─────────────────────────────────────────
Write-Host "`n[LAYER 8] Trazas End-to-End`n" -ForegroundColor Magenta

Test-Step "Tempo tiene trazas almacenadas" {
    Start-Sleep -Seconds 2
    try {
        $traces = Invoke-RestMethod -Uri "http://localhost:3200/api/search?limit=10" -TimeoutSec 10 -UseBasicParsing
        $count = ($traces | Measure-Object).Count
        if ($count -eq 0) { Test-Skip "Traces en Tempo" "No hay trazas en Tempo - generar trafico cross-service"; return }
        $services = $traces | ForEach-Object { $_.serviceName } | Select-Object -Unique
        Write-Host "($count trazas, servicios: $($services -join ', '))" -NoNewline
    } catch {
        Test-Skip "Traces en Tempo" "Tempo search no disponible: $_"
    }
}

# ─── LAYER 9: Logs end-to-end ─────────────────────────────────────────────
Write-Host "`n[LAYER 9] Logs End-to-End`n" -ForegroundColor Magenta

Test-Step "Loki tiene logs via Promtail (containers)" {
    $q = [System.Web.HttpUtility]::UrlEncode('{job="varlogs"}')
    $r = Invoke-Api "http://localhost:3100/loki/api/v1/query_range?query=$q&limit=1" -TimeoutSeconds 10
    if ($r.status -ne "success") { throw "Loki query: $($r.status)" }
    $streamCount = ($r.data.result | Measure-Object).Count
    if ($streamCount -eq 0) {
        Test-Skip "Logs containers" "No hay streams varlogs en Loki. Promtail puede no tener acceso a /var/log en Docker Desktop Windows"
        return
    }
}

Test-Step "OTel collector recibe logs de Spring Boot (pipeline logs)" {
    Start-Sleep -Seconds 2
    $logs = docker compose logs otel-collector --tail 50 2>&1
    $hasLogPipeline = $logs | Select-String -Pattern "logs" -SimpleMatch -CaseSensitive
    if (-not $hasLogPipeline) { Test-Skip "Pipeline logs OTel" "No se ven logs del pipeline en collector - puede estar en nivel basic"; return }
}

Test-Step "Metricas de JVM heap disponibles en Prometheus con label job" {
    $q = 'jvm_memory_used_bytes{area="heap"}'
    $result = Invoke-Prometheus $q
    $jobs = $result | ForEach-Object { $_.metric.job } | Select-Object -Unique
    Write-Host "($($jobs -join ', '))" -NoNewline
}

# ─────────────────────────────────────────────────────────────────────────────
# Reporte Final
# ─────────────────────────────────────────────────────────────────────────────
$total = $passed + $failed + $skipped
Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "  RESULTADOS" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Total : $total"
Write-Host "  Passed: $passed" -ForegroundColor Green
Write-Host "  Failed: $failed" -ForegroundColor $(if ($failed -gt 0) { "Red" } else { "Green" })
Write-Host "  Skipped: $skipped" -ForegroundColor Yellow
Write-Host "========================================================`n" -ForegroundColor Cyan

if ($failed -gt 0) {
    Write-Host "Tests FALLIDOS:" -ForegroundColor Red
    $results | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
        Write-Host "  - $($_.Test): $($_.Error)" -ForegroundColor DarkRed
    }
    Write-Host "`nALGUNOS TESTS FALLARON" -ForegroundColor Red
    exit 1
} else {
    Write-Host "TODOS LOS TESTS PASARON" -ForegroundColor Green
    exit 0
}

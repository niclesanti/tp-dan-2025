# 🚀 CI/CD Configuration - GitHub Actions

## ✅ Workflows Disponibles

### 1. `ci.yml` - Tests Simples (Recomendado para comenzar)

**Características**:
- ✅ Ejecuta tests de **user-svc** y **gestion-svc** en jobs paralelos
- ✅ Dos jobs independientes (uno por servicio)
- ✅ Ideal para proyectos en desarrollo
- ⏱️ Tiempo: ~2-3 minutos (paralelo)

**Cuándo se ejecuta**:
- Push a `develop` o `main`
- Pull Request a `develop` o `main`

**Qué hace**:
1. **Job `test`** (user-svc): Levanta MySQL, ejecuta `./mvnw clean test -pl services/user-svc -am -B`
2. **Job `test-gestion-svc`** (gestion-svc): Testcontainers levanta PostgreSQL + RabbitMQ automáticamente, ejecuta `./mvnw clean test -pl services/gestion-svc -am -B`
3. Ambos jobs publican resultados de tests con `dorny/test-reporter`

**Nota**: reservas-svc se agregará al workflow cuando esté completo.

### 2. `ci-parallel.yml` - Tests Paralelos (Avanzado - Opcional)

**Características**:
- ⚡ Tests en paralelo por servicio
- 📊 Reportes de cobertura individuales
- 🎯 Matrix strategy
- ⏱️ Tiempo: ~2-3 minutos (más rápido)

**Qué hace**:
1. **Build**: Compila todo el proyecto una vez
2. **Test (paralelo)**: Ejecuta tests de cada servicio en jobs separados
3. **Summary**: Resume resultados

**Ventajas**:
- Tests 2-3x más rápidos (ejecución paralela)
- Fallas aisladas por servicio
- Reportes de cobertura individuales

---

## 🔧 Cambios Realizados al Workflow Original

### ❌ Workflow Original (No funcionaba)

```yaml
- name: Dar permisos de ejecución a mvnw
  run: chmod +x backend/mvnw  # ❌ backend/ no existe

- name: Ejecutar tests
  run: |
    cd backend                 # ❌ backend/ no existe
    ./mvnw clean test
```

**Problemas**:
- Buscaba `backend/` (no existe en este proyecto)
- No consideraba estructura multi-módulo
- No tenía soporte de base de datos

### ✅ Workflow Corregido (Funciona)

```yaml
services:
  mysql:                       # ✅ MySQL como servicio
    image: mysql:8.3
    # ...

- name: Dar permisos de ejecución a mvnw
  run: chmod +x mvnw          # ✅ mvnw en la raíz

- name: Ejecutar tests de user-svc
  run: ./mvnw clean test -pl services/user-svc -am -B   # ✅ Solo user-svc + dependencias
  env:
    MYSQL_HOST: localhost     # ✅ Variables de entorno
```

**Mejoras**:
- Ejecuta desde la raíz con POM padre
- Levanta MySQL automáticamente
- Tests solo de user-svc (desarrollo incremental)
- Flag `-am` (also-make) incluye dependencias (dan-common-lib)
- Reporta resultados

---

## 📊 Estructura del Proyecto

```
tp-dan-2025/
├── pom.xml                    # ← POM PADRE (ejecutar aquí)
├── mvnw                       # ← Maven wrapper
├── services/
│   ├── user-svc/
│   │   ├── pom.xml           # ← Módulo hijo
│   │   ├── mvnw              # ← Maven wrapper propio
│   │   └── src/test/...
│   ├── gestion-svc/
│   │   ├── pom.xml
│   │   ├── mvnw
│   │   └── src/test/...
│   └── reservas-svc/
│       ├── pom.xml
│       ├── mvnw
│       └── src/test/...
└── common/
    └── dan-common-lib/
        └── pom.xml
```

**POM Padre define módulos**:
```xml
<modules>
  <module>services/user-svc</module>
  <module>services/gestion-svc</module>
  <module>services/reservas-svc</module>
  <module>common/dan-common-lib</module>
</modules>
```

---

## 🎯 Opciones de Ejecución

### Opción 1: Solo user-svc (Actualmente en CI)
```bash
./mvnw clean test -pl services/user-svc -am
```
Ejecuta tests de **user-svc** y sus dependencias (dan-common-lib).
- `-pl services/user-svc` = project list (especifica el módulo)
- `-am` = also-make (incluye dependencias)

### Opción 2: Tests de un servicio específico
```bash
cd services/user-svc
./mvnw test
```
Solo ejecuta tests de ese servicio (sin dependencias).

### Opción 3: Tests de múltiples servicios específicos
```bash
./mvnw test -pl services/user-svc,services/gestion-svc -am
```
Ejecuta tests de módulos seleccionados + dependencias.

### Opción 4: Todos los tests
```bash
./mvnw clean test
```
Ejecuta tests de **todos** los módulos secuencialmente.

---

## 🔍 Cómo Verificar que Funciona

### Localmente (antes de hacer push)

```powershell
# 1. Asegúrate de que MySQL esté corriendo
docker compose up -d mysql

# 2. Ejecuta tests como lo haría CI (solo user-svc)
./mvnw clean test -pl services/user-svc -am -B

# Deberías ver algo como:
# [INFO] tp2025 ......................................... SUCCESS
# [INFO] dan-common-lib ................................. SUCCESS
# [INFO] user-svc ....................................... SUCCESS
# [INFO] BUILD SUCCESS
```

### En GitHub Actions

1. Haz un commit y push:
```bash
git add .github/workflows/ci.yml
git commit -m "fix: corregir workflow CI para multi-módulo"
git push origin develop
```

2. Ve a GitHub → Actions tab → Verás el workflow corriendo

3. Revisa los logs:
   - ✅ Verde = Todo OK
   - ❌ Rojo = Tests fallaron

---

## 📋 Servicios de GitHub Actions

### MySQL Service Container

```yaml
services:
  mysql:
    image: mysql:8.3
    env:
      MYSQL_ROOT_PASSWORD: rootpwd
      MYSQL_DATABASE: users
      MYSQL_USER: usr_app
      MYSQL_PASSWORD: usrapp
    ports:
      - 3306:3306
    options: >-
      --health-cmd="mysqladmin ping -h localhost"
      --health-interval=10s
      --health-timeout=5s
      --health-retries=5
```

**Qué hace**:
- Levanta MySQL 8.3 para los tests
- Configura base de datos `users` con credenciales
- Health check para asegurar que esté listo antes de correr tests
- Se destruye automáticamente al terminar

---

## 🔐 Variables de Entorno Necesarias

Los tests necesitan estas variables (ya configuradas en el workflow):

```yaml
env:
  MYSQL_HOST: localhost
  MYSQL_DATABASE: users
  MYSQL_USER: usr_app
  MYSQL_PASSWORD: usrapp
```

**Nota**: Para producción, usa GitHub Secrets en vez de valores hardcodeados.

---

## 📈 Reportes de Tests

### Test Reporter (incluido)

```yaml
- name: Publicar resultados de tests
  uses: dorny/test-reporter@v1
  with:
    name: Resultados de Tests Maven
    path: '**/target/surefire-reports/TEST-*.xml'
    reporter: java-junit
```

**Qué proporciona**:
- ✅ Resumen visual de tests en el PR
- ✅ Tests fallidos destacados
- ✅ Historial de tests

---

## 🔄 Cómo Agregar Más Servicios al CI

El CI ya ejecuta **user-svc** y **gestion-svc**. Cuando reservas-svc esté listo, agrégualo así:

### Estado actual

| Servicio | CI | Estrategia de test |
|---|---|---|
| **user-svc** | ✅ Job `test` | MySQL service container en GitHub Actions |
| **gestion-svc** | ✅ Job `test-gestion-svc` | Testcontainers (PostgreSQL + RabbitMQ) |
| **reservas-svc** | ⬜ Pendiente | Testcontainers (MongoDB + RabbitMQ) |

### Paso 1: Preparar el servicio localmente

✅ **gestion-svc** (PostgreSQL + RabbitMQ) — **Ya implementado**:
- `src/test/resources/application-test.properties` con H2 en modo PostgreSQL
- `GestionSvcApplicationTests` con `@Testcontainers` + `PostgreSQLContainer` + `RabbitMQContainer`
- Verifica con: `./mvnw test -pl services/gestion-svc -am`

**Para reservas-svc** (MongoDB):
1. Asegúrate de tener configuración de test con embedded MongoDB:
   ```properties
   spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
   ```

2. Agrega Flapdoodle al `pom.xml`:
   ```xml
   <dependency>
       <groupId>de.flapdoodle.embed</groupId>
       <artifactId>de.flapdoodle.embed.mongo</artifactId>
       <version>4.9.3</version>
       <scope>test</scope>
   </dependency>
   ```

3. Verifica localmente:
   ```bash
   ./mvnw test -pl services/reservas-svc -am
   ```

### Paso 2: Actualizar el workflow

En [.github/workflows/ci.yml](.github/workflows/ci.yml), cambia:

```yaml
# Antes (solo user-svc)
- name: Ejecutar tests de user-svc
  run: ./mvnw clean test -pl services/user-svc -am -B
```

A:

```yaml
# Después (múltiples servicios)
- name: Ejecutar tests de user-svc, gestion-svc y reservas-svc
  run: ./mvnw clean test -pl services/user-svc,services/gestion-svc,services/reservas-svc -am -B
```

O para todos los módulos:

```yaml
# Todos los módulos
- name: Ejecutar tests de todos los módulos
  run: ./mvnw clean test -B
```

### Paso 3: Agregar servicios adicionales si es necesario

Si gestion-svc necesita PostgreSQL en CI:

```yaml
services:
  mysql:
    # ... (ya existe)
  
  postgres:
    image: postgres:16
    env:
      POSTGRES_DB: appdb
      POSTGRES_USER: appuser
      POSTGRES_PASSWORD: apppwd
    ports:
      - 5432:5432
    options: >-
      --health-cmd="pg_isready -U appuser"
      --health-interval=10s
      --health-timeout=5s
      --health-retries=5
```

---

## 🎓 Mejoras Futuras (Opcionales)

### 1. Agregar Cobertura de Código

```yaml
- name: Generar reporte de cobertura
  run: ./mvnw jacoco:report

- name: Subir a Codecov
  uses: codecov/codecov-action@v4
  with:
    files: '**/target/site/jacoco/jacoco.xml'
```

### 2. Cacheo de Dependencias (ya incluido)

```yaml
- uses: actions/setup-java@v4
  with:
    cache: 'maven'  # ← Cachea ~/.m2/repository
```

### 3. Build de Docker Images en CI

```yaml
- name: Build Docker images
  run: |
    docker compose build user-svc
    docker compose build gestion-svc
```

### 4. Deploy Automático

```yaml
deploy:
  needs: test
  if: github.ref == 'refs/heads/main'
  steps:
    - name: Deploy to staging
      run: # ... deploy commands
```

---

## ⚠️ Troubleshooting

### Tests fallan en CI pero pasan localmente

**Posibles causas**:
1. **MySQL no disponible**: Revisa que el service container esté healthy
2. **Variables de entorno**: Verifica que estén configuradas
3. **Timeout**: Tests pueden tardar más en CI (menos recursos)

**Solución**:
```yaml
# Aumentar timeout si es necesario
- name: Ejecutar tests
  run: ./mvnw test -B
  timeout-minutes: 15  # Default: 360
```

### "Permission denied: mvnw"

**Solución**: Ya está agregado en el workflow
```yaml
- name: Dar permisos de ejecución a mvnw
  run: chmod +x mvnw
```

### Tests requieren MongoDB/PostgreSQL

**Solución**: Agregar más service containers
```yaml
services:
  mysql:
    # ... existing

  mongodb:
    image: mongo:7
    env:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: rootpwd
    ports:
      - 27017:27017

  postgres:
    image: postgres:16
    env:
      POSTGRES_DB: appdb
      POSTGRES_USER: appuser
      POSTGRES_PASSWORD: apppwd
    ports:
      - 5432:5432
```

---

## ✅ Checklist para PR

Antes de crear tu PR, verifica:

- [ ] Workflow CI está actualizado (`.github/workflows/ci.yml`)
- [ ] Tests pasan localmente (`./mvnw clean test`)
- [ ] MySQL está corriendo (`docker compose up -d mysql`)
- [ ] Commit y push han gatillado el workflow
- [ ] Workflow en GitHub Actions está Verde ✅

---

## 📚 Referencias

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Maven in GitHub Actions](https://docs.github.com/en/actions/guides/building-and-testing-java-with-maven)
- [Service Containers](https://docs.github.com/en/actions/using-containerized-services)

---

**¡Todo listo para CI/CD automatizado!** 🚀

Ahora cada PR a `develop` o `main` ejecutará automáticamente todos los tests.

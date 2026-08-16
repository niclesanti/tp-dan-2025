# Sistema de Gestión Hotelera — TP DAN 2025

Trabajo Práctico de la materia **Desarrollo de Aplicaciones en la Nube** — UTN FRSF ISI, 2025 (finalizado agosto 2026).

Sistema de gestión hotelería basado en **arquitectura de microservicios** distribuida en la nube, con frontend SPA, service discovery, API gateway, mensajería asincrónica y stack completo de observabilidad.

## Autores

| Nombre | GitHub |
|--------|--------|
| Nicle, Santiago | [@niclesanti](https://github.com/niclesanti) |
| Meichtry, Victoria | [@vmeichtry](https://github.com/vmeichtry) |

## Profesores

- Barragán, Pablo
- Dominguez, Martin

---

## Arquitectura

```
                         ┌─────────────┐
                         │   Frontend  │  :5173 (React SPA)
                         └──────┬──────┘
                                │
                    ┌───────────▼───────────┐
                    │  Spring Cloud Gateway │  :8080
                    │  (Load Balancer)      │
                    └───────────┬───────────┘
                                │
              ┌─────────────────┼──────────────────┐
              │                 │                  │
     ┌────────▼────────┐  ┌─────▼──────┐  ┌────────▼────────┐
     │    user-svc     │  │reservas-svc│  │   gestion-svc   │
     │    :8081        │  │  :8082     │  │     :8083       │
     │  MySQL (JPA)    │  │ MongoDB    │  │ PostgreSQL (JPA)│
     └─────────────────┘  └─────┬──────┘  └────────┬────────┘
                                │                  │
                         ┌──────▼──────┐     ┌─────▼──────┐
                         │  RabbitMQ   │◄────│  Publisher │
                         └─────────────┘     └────────────┘
              │
     ┌────────▼────────┐
     │  Eureka Server  │  :8761 (Service Discovery)
     └─────────────────┘

     ┌─────────────────────────────────────────────────┐
     │            Observability Stack                  │
     │  OTel Collector → Prometheus / Tempo / Loki     │
     │                     └──→ Grafana (:3000)        │
     └─────────────────────────────────────────────────┘
```

### Módulos

| Módulo | Path | Base de datos | Rol |
|--------|------|---------------|-----|
| `user-svc` | `services/user-svc` | MySQL 8.3 | CRUD de usuarios (huéspedes y propietarios), tarjetas de crédito, bancos |
| `gestion-svc` | `services/gestion-svc` | PostgreSQL 16 | Hoteles, habitaciones, tarifas; publica eventos a RabbitMQ |
| `reservas-svc` | `services/reservas-svc` | MongoDB 7 | Búsqueda de disponibilidad, lifecycle de reservas, pagos; consume eventos RabbitMQ |
| `dan-common-lib` | `common/dan-common-lib` | — | DTOs y eventos compartidos entre servicios |
| `dan-spring-gateway` | `common/dan-spring-gateway` | — | API Gateway (Spring Cloud Gateway MVC) |
| `dan-eureka-server` | `common/dan-eureka-server` | — | Service discovery (Netflix Eureka) |
| `frontend` | `frontend/` | — | SPA (React 19, Vite 8, Tailwind 4, shadcn/ui) |

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21, Spring Boot 3.5, Spring Cloud 2025 |
| Persistencia | MySQL 8.3, PostgreSQL 16, MongoDB 7 |
| Mensajería | RabbitMQ 3 (management plugin) |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway MVC |
| Frontend | React 19, TypeScript 6, Vite 8, Tailwind CSS 4, shadcn/ui |
| State Management | TanStack Query v5 |
| Formularios | React Hook Form + Zod |
| Contenedores | Docker, Docker Compose (multi-layer: base + dev + prod) |
| Observability | OpenTelemetry, Prometheus, Grafana, Tempo, Loki, Promtail |
| Testing | JUnit 5, Testcontainers, JaCoCo (95% branch coverage) |
| Build | Maven (multi-module reactor), npm |

---

## Requisitos previos

### Backend
- **Java 21** — [Adoptium Temurin 21](https://adoptium.net/es/temurin/releases/?version=21&package=jdk)
- **Docker Desktop** — [Instalar Docker](https://docs.docker.com/desktop/setup/install/windows-install/)
- **Maven** (incluido via wrapper `./mvnw`)

### Frontend
- **Node.js 22** — [Descargar Node](https://nodejs.org/es/download)

### Herramientas (opcionales)
- **VS Code** — [Descargar](https://code.visualstudio.com/)
- **Git** — [Descargar](https://git-scm.com/)

---

## Inicio rápido

### Stack completo (recomendado)

```bash
# Clonar el repositorio
git clone git@github.com:niclesanti/tp-dan-2025.git
cd tp-dan-2025

# Crear archivo .env (copiar desde .env.example)
cp .env.example .env

# Levantar todo el stack (microservicios + infra + observabilidad + frontend)
docker compose up -d --build

# Verificar que todos los servicios estén healthy
docker compose ps
```

### Verificar que funciona

```bash
# Gateway responde
curl http://localhost:8080/users/bancos

# Frontend
# Abrir http://localhost:5173

# Eureka Dashboard
# Abrir http://localhost:8761

# Grafana (admin/admin)
# Abrir http://localhost:3000
```

### Comandos útiles (Makefile)

```bash
make dev-up          # Levantar stack completo
make dev-down        # Detener todo
make test            # Ejecutar tests de user-svc
make health          # Verificar health de servicios
```

### Desarrollo local (IDE)

```bash
# 1. Levantar infraestructura base
docker compose up -d mysql postgres mongodb rabbitmq

# 2. Compilar la librería compartida primero
cd common/dan-common-lib && ./mvnw clean install -DskipTests

# 3. Ejecutar cada microservicio desde el IDE
#    Usar Spring Profile: local

# 4. Frontend
cd frontend && npm install && npm run dev
```

---

## Servicios y puertos

### Puertos de desarrollo (dev)

| Servicio | Puerto Host | Puerto Interno | URL |
|----------|-------------|----------------|-----|
| **Frontend** | 5173 | 80 | http://localhost:5173 |
| **API Gateway** | 8080 | 8080 | http://localhost:8080 |
| **user-svc** | 8081 | 8080 | http://localhost:8081 |
| **reservas-svc** | 8082 | 8080 | http://localhost:8082 |
| **gestion-svc** | 8083 | 8080 | http://localhost:8083 |
| **Eureka Server** | 8761 | 8761 | http://localhost:8761 |

### Infraestructura

| Servicio | Puerto Host | Uso |
|----------|-------------|-----|
| MySQL | 3307 | Base de datos de user-svc |
| PostgreSQL | 5433 | Base de datos de gestion-svc |
| MongoDB | 27018 | Base de datos de reservas-svc |
| RabbitMQ AMQP | 5673 | Mensajería |
| RabbitMQ UI | 15673 | Panel de administración |
| PHPMyAdmin | 6080 | Admin MySQL |
| PgAdmin | 6081 | Admin PostgreSQL |
| Mongo Express | 6091 | Admin MongoDB |

### Observabilidad

| Servicio | Puerto | Uso |
|----------|--------|-----|
| Grafana | 3000 | Dashboards (admin/admin) |
| Prometheus | 9090 | Métricas |
| Tempo | 3200 | Trazas distribuidas |
| Loki | 3100 | Agregación de logs |
| OTel Collector (OTLP HTTP) | 4318 | Recepción de telemetría |
| OTel Collector (OTLP gRPC) | 4317 | Recepción de telemetría |

---

## Funcionalidades

### Etapa 1 — Usuarios y Bancos
- CRUD de huéspedes (registro con tarjeta de crédito principal)
- CRUD de propietarios (registro con cuenta bancaria)
- Gestión de tarjetas de crédito (agregar, eliminar, cambiar principal)
- CRUD de bancos
- Búsqueda de usuarios por nombre o DNI
- Borrado en cascada de tarjetas al eliminar huésped

### Etapa 2 — Hoteles, Habitaciones, Tarifas y Reservas
- CRUD de hoteles, habitaciones y tarifas
- Publicación de eventos de habitaciones/tarifas vía RabbitMQ
- Búsqueda de habitaciones disponibles
- Lifecycle de reservas: creación, consulta, actualización de estado, pago
- Consumo de eventos RabbitMQ en reservas-svc

### Etapa 3 — API Gateway
- Spring Cloud Gateway MVC como punto de entrada único
- Enrutamiento por path a cada microservicio
- StripPrefix para reescritura de URLs transparente

### Etapa 4 — Service Discovery
- Netflix Eureka Server para descubrimiento automático de servicios
- Load balancing vía `lb://service-name` en el gateway
- Escalado horizontal con `docker compose --scale`
- Endpoints de información (`/info`) en cada servicio

### Etapa 5 — Observabilidad y Testing
- Stack completo: OpenTelemetry → Prometheus + Tempo + Loki → Grafana
- Métricas JVM, HTTP, conexiones DB
- Trazas distribuidas entre microservicios
- Logs correlacionados con trace IDs
- Dashboard de Grafana para Spring Boot Microservices
- JaCoCo con 95% de cobertura branch en controllers y services
- Testcontainers para integración con MySQL, PostgreSQL, MongoDB y RabbitMQ

---

## Arquitectura del frontend

```
frontend/src/
├── components/
│   ├── ui/              # shadcn/ui (25 componentes)
│   ├── layout/          # AppLayout, Sidebar, Header
│   ├── bancos/          # CRUD bancos
│   ├── usuarios/        # CRUD huéspedes y propietarios
│   ├── hoteles/         # CRUD hoteles, habitaciones, amenities
│   ├── tarifas/         # CRUD tarifas
│   └── reservas/        # Búsqueda, reservas, pagos, reviews
├── hooks/               # TanStack Query hooks
├── lib/validators/      # Schemas Zod
├── services/            # Llamadas API (Axios)
├── types/               # Interfaces TypeScript
└── routes/              # React Router v7
```

### Secciones implementadas

| Sección | Ruta | Descripción |
|---------|------|-------------|
| Reservas | `/` | Búsqueda de habitaciones y gestión de reservas |
| Hoteles | `/hoteles` | Hoteles y habitaciones |
| Tarifas | `/tarifas` | Gestión de tarifas |
| Usuarios | `/usuarios` | Huéspedes y propietarios |
| Bancos | `/bancos` | Gestión de bancos |
| Login | `/login` | autenticación |

---

## Perfiles de ejecución

| Perfile | Propiedades | Uso |
|---------|------------|-----|
| `dev` | `application-dev.properties` | Por defecto en Docker; Swagger ON, verbose SQL |
| `prod` | `application-prod.properties` | Swagger OFF, JVM optimizado, actuator limitado |
| `local` | `application-local.properties` | Ejecución desde IDE con hosts DB en localhost |

---

## Docker Compose — Capas

El proyecto usa **3 archivos compose** apilados:

| Archivo | Propósito | Se aplica con |
|---------|-----------|---------------|
| `docker-compose.yml` | Configuración base: todos los servicios e infraestructura | `docker compose up` |
| `docker-compose.override.yml` | Dev: puertos host, debug JVM, hot-reload | Automático con `up` |
| `docker-compose.prod.yml` | Producción: sin puertos host, resource limits, restart policies | `-f docker-compose.yml -f docker-compose.prod.yml` |

---

## Entorno (.env)

El archivo `.env` (gitignored) define las variables de entorno para Docker Compose:

```bash
# MySQL
MYSQL_DATABASE=users
MYSQL_USER=usr_app
MYSQL_PASSWORD=usrapp
MYSQL_ROOT_PASSWORD=rootpwd

# PostgreSQL
POSTGRES_DB=appdb
POSTGRES_USER=appuser
POSTGRES_PASSWORD=apppwd

# RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=admin

# MongoDB
MONGO_USER=root
MONGO_PASSWORD=rootpwd
MONGO_DB=reservas

# Spring
SPRING_PROFILE=dev,eureka
```

Copiar `.env.example` como `.env` y ajustar valores para producción.

---

## Documentación por etapas

| Etapa | Archivo | Descripción |
|-------|---------|-------------|
| Etapa 01 | [ETAPA01.md](./ETAPA01.md) | Usuarios, tarjetas de crédito, bancos |
| Etapa 02 | [ETAPA02.md](./ETAPA02.md) | Hoteles, habitaciones, tarifas, reservas, Docker |
| Etapa 03 | [ETAPA03.md](./ETAPA03.md) | Spring Cloud Gateway |
| Etapa 04 | [ETAPA04.md](./ETAPA04.md) | Eureka Server y Client |
| Etapa 05 | [ETAPA05-TESTING.md](./ETAPA05-TESTING.md) | Verificación de observabilidad |
| Etapa 05 | [ETAPA05-OBSERVABILIDAD.md](./ETAPA05-OBSERVABILIDAD.md) | Stack de observabilidad |

---

## Licencia

Este proyecto es un trabajo práctico académico de la UTN FRSF ISI.

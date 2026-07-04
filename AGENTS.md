# AGENTS.md — TP DAN 2025

Hotel reservation microservice system. Maven multi-module (Spring Boot 3.5, Java 21).

## Modules

| Module | Path | DB | Messaging |
|--------|------|----|-----------|
| `user-svc` | `services/user-svc` | MySQL (JPA+JDBC) | — |
| `gestion-svc` | `services/gestion-svc` | PostgreSQL (JPA+JDBC) | RabbitMQ publisher |
| `reservas-svc` | `services/reservas-svc` | MongoDB (Spring Data) | RabbitMQ consumer |
| `dan-common-lib` | `common/dan-common-lib` | — | Shared DTOs/events |

## Quick commands

```powershell
# Full stack (builds images from source)
docker compose up -d --build

# Same via Makefile
make dev-up

# Run tests for one module
cd services/user-svc && ./mvnw test

# Makefile shortcut (runs user-svc tests only)
make test

# Local dev from IDE (outside Docker)
./mvnw spring-boot:run -DskipTests -Dspring-boot.run.profiles=local
```

## Architecture

- **user-svc** (`:8081`): Users (huésped/propietario), credit cards, banks — CRUD via REST
- **gestion-svc** (`:8083`): Hotels, rooms, rates — publishes events to RabbitMQ exchange `dan.exchange` with routing key `dan.habitacion.#`
- **reservas-svc** (`:8082`): Room search, reservation lifecycle, payments — consumes RabbitMQ events (`spring.amqp.deserialization.trust.all=true`)
- **dan-common-lib**: DTOs (`HotelDTO`, `HabitacionDTO`, `TarifaDTO`) and events (`HabitacionEvent`, `HotelCierreEvent`) — must be compiled first for dependent services

## Key gotchas

- **Docker context must be repo root** — all Dockerfiles build from project root
- **gestion-svc & reservas-svc depend on dan-common-lib** — their Dockerfiles copy ALL module POMs to satisfy the Maven reactor, and build with `-pl <module> -am` (also builds dan-common-lib)
- **CRLF→LF** — their Dockerfiles run `sed -i 's/\r$//' mvnw` to fix Windows line endings
- **.env is gitignored** — never commit it; use `.env.prod.template` for production
- **`.env` values used by docker compose** — MYSQL_*, POSTGRES_*, RABBITMQ_*, MONGO_* env vars defined there

## Testing

- JaCoCo enforces **95% branch coverage** on `**/controller/*` and `**/service/*` in every service — `mvn test` will fail if coverage drops below threshold
- Testcontainers used for MySQL, PostgreSQL, MongoDB, RabbitMQ integration tests
- H2 used in some test scopes as lightweight alternative

## Profiles

| Profile | Properties file | Use |
|---------|---------------|-----|
| `dev` | `application-dev.properties` | Default in Docker; Swagger ON, verbose SQL, full actuator |
| `prod` | `application-prod.properties` | Swagger OFF, optimized JVM, limited actuator |
| `local` | `application-local.properties` | For IDE execution with localhost DB hosts |

## Ports (development)

| Service | Host port |
|---------|-----------|
| user-svc API | 8081 |
| reservas-svc API | 8082 |
| gestion-svc API | 8083 |
| PHPMyAdmin | 6080 |
| PgAdmin | 6081 |
| Mongo Express | 6091 |
| RabbitMQ UI | 15672 |
| Debug (each svc) | 5005 / 5006 / 5007 |

RabbitMQ AMQP port 5672, MySQL 3306, PostgreSQL 5433, MongoDB 27017.

## Code conventions

- **MapStruct + Lombok**: annotation processors configured in `maven-compiler-plugin` (`lombok`, `mapstruct-processor`, `lombok-mapstruct-binding`)
- `spring.jpa.hibernate.ddl-auto=none` — schema managed via init SQL scripts in `infra/<db>/initdb/`
- RabbitMQ exchange and routing key configured per service via `rabbitmq.exchange` and `rabbitmq.routingkey` properties

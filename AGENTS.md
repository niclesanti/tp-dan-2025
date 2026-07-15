# AGENTS.md — TP DAN 2025

Hotel reservation microservice system. Maven multi-module (Spring Boot 3.5, Java 21).

## Modules

| Module | Path | DB | Messaging |
|--------|------|----|-----------|
| `user-svc` | `services/user-svc` | MySQL (JPA+JDBC) | — |
| `gestion-svc` | `services/gestion-svc` | PostgreSQL (JPA+JDBC) | RabbitMQ publisher |
| `reservas-svc` | `services/reservas-svc` | MongoDB (Spring Data) | RabbitMQ consumer |
| `dan-common-lib` | `common/dan-common-lib` | — | Shared DTOs/events |
| `dan-spring-gateway` | `common/dan-spring-gateway` | — | Spring Cloud Gateway MVC |
| `dan-eureka-server` | `common/dan-eureka-server` | — | Netflix Eureka |
| `frontend` | `frontend/` | — | React SPA (Vite + Tailwind) |

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
- **dan-spring-gateway** (`:8080`): API Gateway, routes requests to services via `lb://service-name` when Eureka is active
- **dan-eureka-server** (`:8761`): Service discovery — start before other services
- **dan-common-lib**: DTOs (`HotelDTO`, `HabitacionDTO`, `TarifaDTO`) and events (`HabitacionEvent`, `HotelCierreEvent`) — must be compiled first for dependent services
- **frontend** (`:5173`): React SPA (Vite + Tailwind + shadcn/ui) — served via nginx in Docker, multi-stage build

## Frontend architecture

React SPA under `frontend/src/`. Stack: React 18, TypeScript 5, Vite, Tailwind CSS, shadcn/ui (base-vega), TanStack Query v5, React Hook Form + Zod, Axios, Lucide icons, sonner toasts.

### Directory structure

```
frontend/src/
├── components/
│   ├── ui/           # shadcn components (button, dialog, input, table, field, select, badge, alert-dialog, spinner, tabs, card, etc.)
│   ├── layout/       # AppLayout, Sidebar, Header
│   ├── bancos/       # BancosPage, BancosSection, BancosTable, BancoFormDialog
│   └── usuarios/     # UsuariosPage, HuespedesTab, PropietariosTab, HuespedFormDialog, PropietarioFormDialog, TarjetasSection, DeleteConfirmDialog
├── hooks/            # TanStack Query hooks (useBancos, useHuespedes, usePropietarios, useTarjetas, etc.)
├── lib/validators/   # Zod schemas (banco.ts, huesped.ts, propietario.ts)
├── services/         # Axios API calls (api.ts, banco.service.ts, usuario.service.ts)
├── types/            # TypeScript interfaces (usuario.ts — includes Banco, Huesped, Propietario, etc.)
├── routes/           # AppRouter.tsx (react-router-dom v7)
└── pages/            # DashboardPage
```

### Implemented sections

| Section | Route | Components | API base path |
|---------|-------|------------|---------------|
| Dashboard | `/` | `DashboardPage` | — |
| Usuarios | `/usuarios` | `UsuariosPage` → `HuespedesTab`, `PropietariosTab` | `/users/users/` |
| Bancos | `/bancos` | `BancosPage` → `BancosSection`, `BancosTable`, `BancoFormDialog` | `/users/bancos` |

### Code patterns

- **Dual create/edit dialogs**: Single `*FormDialog` component handles both modes via optional entity prop (`entity?: Entity | null`). Two separate `useForm` instances when create has extra required fields.
- **State management**: `useState` for `createOpen`, `editTarget`, `deleteTarget` (null = closed). Dialog `onOpenChange` clears target on close.
- **Mutation pattern**: Hooks return `useMutation` objects. `onSuccess` in component closes dialog; `onSuccess` in hook invalidates queries and shows toast.
- **Search**: `UsuariosSearchBar` with 300ms debounce for server-side; `useMemo` for client-side filtering (small lists like bancos).
- **Delete confirmation**: Reusable `DeleteConfirmDialog` component (AlertDialog).
- **API calls**: All via gateway at `localhost:8080/users/...`. Service files are plain objects with methods calling `api.get/post/put/delete`.
- **Routing**: Routes defined in `AppRouter.tsx` inside `<AppLayout />`. Sidebar entries in `Sidebar.tsx`.

### Adding a new section

1. Add types to `types/usuario.ts` (or new file)
2. Create `services/<entity>.service.ts` with CRUD methods
3. Create `lib/validators/<entity>.ts` with Zod schema
4. Create `hooks/use<Entities>.ts` with query + mutations
5. Create `components/<entity>/` folder with `*Page.tsx`, `*Section.tsx`, `*Table.tsx`, `*FormDialog.tsx`
6. Add route in `AppRouter.tsx`
7. Sidebar entry already exists in `Sidebar.tsx` if linked from design

## Key gotchas

- **Docker context must be repo root** — all Dockerfiles build from project root
- **gestion-svc & reservas-svc depend on dan-common-lib** — their Dockerfiles copy ALL module POMs to satisfy the Maven reactor, and build with `-pl <module> -am` (also builds dan-common-lib)
- **CRLF→LF** — their Dockerfiles run `sed -i 's/\r$//' mvnw` to fix Windows line endings
- **.env is gitignored** — never commit it; use `.env.prod.template` for production
- **`.env` values used by docker compose** — MYSQL_*, POSTGRES_*, RABBITMQ_*, MONGO_* env vars defined there
- **All services expose8080 internally** — Docker maps to host ports via `docker-compose.override.yml`
- **Default profile is `dev,eureka`** — services register with Eureka; gateway routes via `lb://`

## Testing

- JaCoCo enforces **95% branch coverage** on `**/controller/*` and `**/service/*` in every service — `mvn test` will fail if coverage drops below threshold
- Testcontainers used for MySQL, PostgreSQL, MongoDB, RabbitMQ integration tests
- H2 used in some test scopes as lightweight alternative
- Test profile uses `create-drop` DDL (separate from main `none` setting)

## Profiles

| Profile | Properties file | Use |
|---------|---------------|-----|
| `dev` | `application-dev.properties` | Default in Docker; Swagger ON, verbose SQL, full actuator |
| `prod` | `application-prod.properties` | Swagger OFF, optimized JVM, limited actuator |
| `local` | `application-local.properties` | For IDE execution with localhost DB hosts |

## Ports (development)

Dev ports come from `docker-compose.override.yml` (auto-applied with `docker compose up`).

| Service | Host port | Internal port |
|---------|-----------|---------------|
| **Frontend** | 5173 | 80 |
| Gateway | 8080 | 8080 |
| user-svc API | 8081 | 8080 |
| reservas-svc API | 8082 | 8080 |
| gestion-svc API | 8083 | 8080 |
| Eureka Server | 8761 | 8761 |
| PHPMyAdmin | 6080 | — |
| PgAdmin | 6081 | — |
| Mongo Express | 6091 | — |
| RabbitMQ AMQP | 5673 | 5672 |
| RabbitMQ UI | 15673 | 15672 |
| MySQL | 3307 | 3306 |
| PostgreSQL | 5433 | 5432 |
| MongoDB | 27018 | 27017 |
| Debug (each svc) | 5005 | 5005 |
| **Observability** | | |
| Grafana | 3000 | 3000 |
| Prometheus | 9090 | 9090 |
| Tempo | 3200 | 3200 |
| Loki | 3100 | 3100 |
| OTel Collector (OTLP HTTP) | 4318 | 4318 |
| OTel Collector (OTLP gRPC) | 4317 | 4317 |
| OTel Collector (Prometheus) | 8889 | 8889 |

## Code conventions

- **MapStruct + Lombok**: annotation processors configured in `maven-compiler-plugin` (`lombok`, `mapstruct-processor`, `lombok-mapstruct-binding`)
- `spring.jpa.hibernate.ddl-auto=none` — schema managed via init SQL scripts in `infra/<db>/initdb/`
- RabbitMQ exchange and routing key configured per service via `rabbitmq.exchange` and `rabbitmq.routingkey` properties
- Observability: OpenTelemetry → OTLP exporter, with Grafana/Loki/Tempo/Prometheus stack (see ETAPA05-OBSERVABILIDAD.md)

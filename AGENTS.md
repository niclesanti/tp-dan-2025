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

# Frontend verification (no test runner; typecheck + build)
cd frontend && npx tsc --noEmit && npm run build

# Local dev from IDE (outside Docker)
./mvnw spring-boot:run -DskipTests -Dspring-boot.run.profiles=local
```

## Architecture

- **user-svc** (`:8081`): Users (huésped/propietario), credit cards, banks — CRUD via REST. Exposes `GET /users/dni/{dni}` (exact DNI lookup) and `GET /users/huesped/tarjeta-principal?dni={dni}` → `TarjetaPrincipalDTO {"numero":"..."}` (primary credit card number of a huésped; 404 if user not found / not a huésped / no primary card)
- **gestion-svc** (`:8083`): Hotels, rooms, rates — publishes events to RabbitMQ exchange `dan.exchange` with routing key `dan.habitacion.#`
- **reservas-svc** (`:8082`): Room search, reservation lifecycle, payments — consumes RabbitMQ events (`spring.amqp.deserialization.trust.all=true`). The embedded `Huesped` document in MongoDB holds `dni`, `nombreApellido`, `email`; reservations are created/updated with a `HuespedDTORequest` (mapped via `HuespedMapper`) and searched **by DNI** via `GET /reservas/huesped/dni/{dni}`
- **dan-spring-gateway** (`:8080`): API Gateway, routes requests to services via `lb://service-name` when Eureka is active
- **dan-eureka-server** (`:8761`): Service discovery — start before other services
- **dan-common-lib**: DTOs (`HotelDTO`, `HabitacionDTO`, `TarifaDTO`) and events (`HabitacionEvent`, `HotelCierreEvent`) — must be compiled first for dependent services
- **frontend** (`:5173`): React SPA (Vite + Tailwind + shadcn/ui) — served via nginx in Docker, multi-stage build

## Frontend architecture

React SPA under `frontend/src/`. Stack: React 19, TypeScript 6, Vite 8, Tailwind CSS 4, shadcn/ui (base-vega, built on `@base-ui/react`), TanStack Query v5, React Hook Form + Zod, Axios 1.x, react-router-dom v7, Lucide icons, sonner toasts.

> Note: `tsconfig.json` no longer defines `baseUrl` (deprecated/removed in TypeScript 6); `paths` (`@/*` → `./src/*`) resolve relative to the config file. `npx tsc --noEmit` and `npm run build` are the frontend verification commands (no unit-test runner is configured).

### Directory structure

```
frontend/src/
├── components/
│   ├── ui/              # shadcn components (24 components — button, dialog, input, table, field, select, badge, alert-dialog, spinner, tabs, card, checkbox, dropdown-menu, label, pagination, popover, scroll-area, separator, sheet, skeleton, textarea, tooltip, avatar, breadcrumb)
│   ├── layout/          # AppLayout, Sidebar, SidebarItem, Header
│   ├── bancos/          # BancosPage, BancosSection, BancosTable, BancoFormDialog
│   ├── usuarios/        # UsuariosPage, HuespedesTab, PropietariosTab, HuespedFormDialog, PropietarioFormDialog, TarjetasSection, TarjetaFormDialog, UsuariosSearchBar, DeleteConfirmDialog
│   ├── hoteles/         # HotelesPage, HotelesTab, HabitacionesTab, HotelFormDialog, HabitacionFormDialog, AmenitiesManager, StarRating
│   ├── tarifas/         # TarifasPage, TarifasTable, TarifaFormDialog
│   └── reservas/        # ReservasPage, BuscarHabitacionesTab, GestionReservasTab, CrearReservaDialog, ReservaDetailDialog, PagoFormDialog, ReviewFormDialog, EstadoBadge
├── hooks/               # TanStack Query hooks (useBancos, useHuespedes, usePropietarios, useTarjetas, useUsuarios, useBuscarUsuarios, useHoteles, useTarifas, useReservas)
├── lib/
│   ├── utils.ts         # cn() utility (clsx + twMerge)
│   └── validators/      # Zod schemas (banco, huesped, propietario, hotel, habitacion, tarifa, reserva)
├── services/            # Axios API calls (api.ts, banco.service.ts, usuario.service.ts, hotel.service.ts, tarifa.service.ts, reserva.service.ts)
├── types/               # TypeScript interfaces (usuario.ts, hotel.ts, reserva.ts)
├── routes/              # AppRouter.tsx (react-router-dom v7)
└── pages/               # LoginPage
```

### Implemented sections

| Section | Route | Components | API base path |
|---------|-------|------------|---------------|
| Reservas | `/` (index) | `ReservasPage` → `BuscarHabitacionesTab`, `GestionReservasTab` | `/reservas` |
| Usuarios | `/usuarios` | `UsuariosPage` → `HuespedesTab`, `PropietariosTab` | `/users/users/` |
| Bancos | `/bancos` | `BancosPage` → `BancosSection`, `BancosTable`, `BancoFormDialog` | `/users/bancos` |
| Hoteles | `/hoteles` | `HotelesPage` → `HotelesTab`, `HabitacionesTab` | `/gestion` |
| Tarifas | `/tarifas` | `TarifasPage` → `TarifasTable`, `TarifaFormDialog` | `/gestion/tarifas` |
| Login | `/login` | `LoginPage` (standalone, no layout) | — |

### Sidebar navigation

5 entries in `Sidebar.tsx`: Reservas (`/`), Hoteles & Habitaciones (`/hoteles`), Tarifas (`/tarifas`), Usuarios (`/usuarios`), Bancos (`/bancos`).

### Routing

Routes defined in `AppRouter.tsx`. All routes except `/login` render inside `<AppLayout />` (sidebar + header + `<Outlet />`). Index route (`/`) renders `ReservasPage`.

### Code patterns

- **Page = Tabs composition**: Most pages (`UsuariosPage`, `HotelesPage`, `ReservasPage`) use `<Tabs variant="line">` to split related CRUD into separate tab views.
- **Dual create/edit dialogs**: Single `*FormDialog` handles both modes via optional entity prop (`entity?: Entity | null`). Two separate `useForm` instances when create has extra required fields (credit card for huésped, bank account for propietario).
- **State management**: `useState` for `createOpen` (boolean), `editTarget` (entity | null), `deleteTarget` (entity | null). Dialog `onOpenChange` clears target on close.
- **Mutation pattern**: Hooks return `useMutation` objects. `onSuccess` in component closes dialog; `onSuccess` in hook invalidates queries and shows toast via `sonner`.
- **Search**: `UsuariosSearchBar` with 300ms debounce for server-side search. `useBuscarUsuarios` smart hook auto-detects DNI (digits ≥ 7) vs name search. `useMemo` for client-side filtering (bancos).
- **Pagination**: `Pagination` component for server-side paginated tables (hoteles, habitaciones, tarifas, reservas). Client-side lists (bancos) use no pagination.
- **Delete confirmation**: Reusable `DeleteConfirmDialog` component (AlertDialog) used across all CRUD sections.
- **API calls**: All via gateway. Service files are plain objects with methods calling `api.get/post/put/delete/patch`. Response interceptor normalizes backend validation errors into readable messages.
- **Routing**: Routes defined in `AppRouter.tsx` inside `<AppLayout />`. Sidebar entries in `Sidebar.tsx`.
- **Reservation guest flow**: `CrearReservaDialog` collects `nombreApellido`, `email`, `dni` (validated `^\d{7,8}$`) and sends them as the nested `huesped` in `ReservaDTORequest`. `GestionReservasTab` searches reservations by **DNI** (`useReservasPorHuesped(dni)` → `GET /reservas/reservas/huesped/dni/{dni}`).
- **Payment restrictions**: `ReservaDetailDialog` shows "Agregar Pago" only when the guest's DNI exists in user-svc (query via `usuarioService.buscarPorDniExacto`) AND the estado is `RESERVADA`/`CONFIRMADA`/`ADEUDADA`. `PagoFormDialog` allows only `TARJETA_CREDITO`/`EFECTIVO`, currency fixed to `USD` (read-only), and when `TARJETA_CREDITO` is selected it auto-fetches the guest's primary card via `usuarioService.obtenerTarjetaPrincipalPorDni(dni)` (reactive `useQuery` with `enabled: open && !!dni && method === "TARJETA_CREDITO"`) and sends it as `nroTarjeta`; EFECTIVO omits the card.

### Types

- `types/usuario.ts`: `Usuario`, `Huesped`, `Propietario`, `TarjetaCredito`, `CuentaBancaria`, `Banco`, `TarjetaPrincipalDTO`, `PageResponse<T>`, plus `*CreateRequest` / `*UpdateRequest` types
- `types/hotel.ts`: `Hotel`, `Habitacion`, `TipoHabitacion`, `Tarifa`, `Amenity` (union of 18 values), `AMENITY_LABELS`, `PageResponse<T>`, plus request types
- `types/reserva.ts`: `ReservaDTOResponse`, `ReservaDTORequest`, `HuespedReserva` (`dni`, `nombreApellido`, `email`), `EstadoReserva` (8 states), `ESTADO_RESERVA_LABELS`, `HabitacionDisponibleDTO`, `Pago`/`PagoDTORequest` (with optional `nroTarjeta`), `Review`, `PageResponse<T>`

### Adding a new section

1. Add types to `types/` (new file per domain: `hotel.ts`, `reserva.ts`, etc.)
2. Create `services/<entity>.service.ts` with CRUD methods using `api.get/post/put/delete/patch`
3. Create `lib/validators/<entity>.ts` with Zod schema + exported form values type
4. Create `hooks/use<Entities>.ts` with TanStack Query hooks (queries + mutations with toast + invalidation)
5. Create `components/<entity>/` folder with `*Page.tsx` (tabs if needed), `*Table.tsx`, `*FormDialog.tsx`
6. Add route in `AppRouter.tsx` inside `<Route element={<AppLayout />}>`
7. Add sidebar entry in `Sidebar.tsx`

## Key gotchas

- **Docker context must be repo root** — all Dockerfiles build from project root
- **gestion-svc & reservas-svc depend on dan-common-lib** — their Dockerfiles copy ALL module POMs to satisfy the Maven reactor, and build with `-pl <module> -am` (also builds dan-common-lib)
- **CRLF→LF** — their Dockerfiles run `sed -i 's/\r$//' mvnw` to fix Windows line endings
- **.env is gitignored** — never commit it; use `.env.prod.template` for production
- **`.env` values used by docker compose** — MYSQL_*, POSTGRES_*, RABBITMQ_*, MONGO_* env vars defined there
- **All services expose8080 internally** — Docker maps to host ports via `docker-compose.override.yml`
- **Default profile is `dev,eureka`** — services register with Eureka; gateway routes via `lb://`
- **`ResponseEntity<String>` bodies are sent as raw `text/plain`** — a bare numeric body like `9874567890987456` is valid JSON and axios parses it as a **JS number** (precision loss for cards > 2^53, and `.slice()` crashes). NEVER return a domain string as `ResponseEntity<String>`; wrap it in a small DTO (e.g. `TarjetaPrincipalDTO { numero }`) so it's proper `application/json`.
- **Dialog-scoped `useQuery` must gate on `open`** — include `open` in `enabled` so queries don't fire while the dialog is closed (e.g. `PagoFormDialog`'s card lookup would run when only the parent `ReservaDetailDialog` was open, crashing it once data arrived).
- **Call all React hooks before early returns** — in components with `if (!x) return null;`, place `useQuery`/`useForm`/`useState` above the early return (Rules of Hooks).
- **Lombok + incremental builds**: after editing Lombok-annotated models, run `./mvnw clean test` (a plain `test` may hit `NoSuchMethodError` from stale `target/classes`).
- **otel-collector**: `infra/otel/otel-collector-config.yaml` must not use a `format` key on the Loki exporter (removed; OTel Collector v0.102+ rejects it and the container restart-loops).

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

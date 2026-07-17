# Flujo Docker — TP DAN 2025

## Desarrollo normal (1 instancia por servicio)

```powershell
docker compose up -d --build
```

- Auto-carga `docker-compose.override.yml`
- Puertos directos habilitados:

| Servicio | URL |
|----------|-----|
| user-svc | `http://localhost:8081` |
| reservas-svc | `http://localhost:8082` |
| gestion-svc | `http://localhost:8083` |
| Gateway | `http://localhost:8080` |
| Eureka | `http://localhost:8761` |

- Swagger accesible por gateway: `http://localhost:8080/<servicio>/swagger-ui.html`
- Todos los servicios se registran en Eureka con el perfil `dev,eureka`.

---

## Escalar un servicio a N instancias

```powershell
docker compose -f docker-compose.yml up -d --scale <servicio>=<N> --no-deps <servicio>
```

**Importante:** se usa `-f docker-compose.yml` **sin** el override para evitar conflictos de puertos host.

### Ejemplos

```powershell
# user-svc a 2 instancias
docker compose -f docker-compose.yml up -d --scale user-svc=2 --no-deps user-svc

# gestion-svc a 3 instancias
docker compose -f docker-compose.yml up -d --scale gestion-svc=3 --no-deps gestion-svc

# Varios servicios a la vez
docker compose -f docker-compose.yml up -d --scale user-svc=2 --scale gestion-svc=2 --no-deps user-svc gestion-svc
```

### Comportamiento

- Las instancias escaladas **no tienen puerto host directo** (solo la 1ra con el override tiene `8081:8080`, etc.)
- Son accesibles entre sí por la red interna de Docker
- El gateway (`http://localhost:8080`) hace **load balancing round-robin** entre todas las instancias via Eureka
- Cada instancia se registra en Eureka con un `instance-id` único

---

## Verificar escalado

```powershell
# Listar contenedores
docker compose ps <servicio>

# Ver instancias en Eureka
curl -s http://localhost:8761/eureka/apps/USER-SVC

# Probar load balancing (alterna entre instancias)
curl -s http://localhost:8080/users/info
```

---

## Volver a 1 instancia (con puertos directos)

```powershell
# 1. Escalar a 1 usando solo docker-compose.yml
docker compose -f docker-compose.yml up -d --scale <servicio>=1 <servicio>

# 2. Re-aplicar override para recuperar puertos host y debug
docker compose up -d
```

O directamente:

```powershell
# Baja todo y levanta de nuevo con override
docker compose down <servicio>
docker compose up -d
```

---

## Resumen de comandos

| Situación | Comando |
|-----------|---------|
| Desarrollo (1 instancia, puertos host) | `docker compose up -d --build` |
| user-svc → 2 instancias | `docker compose -f docker-compose.yml up -d --scale user-svc=2 --no-deps user-svc` |
| gestion-svc → 3 instancias | `docker compose -f docker-compose.yml up -d --scale gestion-svc=3 --no-deps gestion-svc` |
| reservas-svc → 2 instancias | `docker compose -f docker-compose.yml up -d --scale reservas-svc=2 --no-deps reservas-svc` |
| Volver a 1 instancia | `docker compose -f docker-compose.yml up -d --scale <svc>=1 <svc>` luego `docker compose up -d` |
| Ver estado | `docker compose ps` |
| Ver logs | `docker compose logs -f <servicio>` |

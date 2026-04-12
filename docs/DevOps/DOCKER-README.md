# 🐳 Docker Setup - TP DAN 2025

Sistema de Gestión Hotelera con arquitectura de microservicios.

## 📋 Prerrequisitos

- [Docker Desktop](https://docs.docker.com/desktop/) (Windows/Mac) o Docker Engine (Linux)
- [Docker Compose](https://docs.docker.com/compose/install/) v2.0+
- Puertos disponibles: `3306`, `5005`, `5006`, `5007`, `5433`, `5672`, `6080`, `6081`, `6091`, `8081`, `8082`, `8083`, `15672`, `27017`

## 🚀 Inicio Rápido

### Desarrollo (Configuración por defecto)

```bash
# Levantar todos los servicios en modo desarrollo
docker compose up -d --build

# Ver logs en tiempo real
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs -f user-svc
```

Servicios disponibles:
- **user-svc API**: http://localhost:8081 (debug: 5005)
- **user-svc Swagger**: http://localhost:8081/swagger-ui
- **gestion-svc API**: http://localhost:8083 (debug: 5006)
- **gestion-svc Swagger**: http://localhost:8083/swagger-ui
- **reservas-svc API**: http://localhost:8082 (debug: 5007)
- **reservas-svc Swagger**: http://localhost:8082/swagger-ui
- **PHPMyAdmin**: http://localhost:6080
- **PgAdmin**: http://localhost:6081
- **Mongo Express**: http://localhost:6091
- **RabbitMQ UI**: http://localhost:15672

### Producción

```bash
# Crear archivo de configuración de producción
cp .env.prod.template .env.prod
# Editar .env.prod con credenciales seguras

# Levantar servicios en modo producción
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

## 🏗️ Arquitectura

```
┌──────────────────────────────────────────────────────────────────────┐
│                         Docker Network (tp-dan-network)              │
│                                                                      │
│  ┌──────────┐  ┌───────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │  MySQL   │  │PHPMyAdmin │  │ user-svc │  │   RabbitMQ       │  │
│  │  :3306   │◄─┤  :6080    │  │  :8081   │  │ :5672 / :15672   │  │
│  └──────────┘  └───────────┘  └──────────┘  └──────────────────┘  │
│       ▲                            │                 ▲    ▲         │
│       └────────────────────────────┘                 │    │         │
│                                                       │    │         │
│  ┌──────────┐  ┌───────────┐  ┌────────────────────────────────┐  │
│  │PostgreSQL│  │  PgAdmin  │  │         gestion-svc            │  │
│  │  :5432   │◄─┤  :6081    │  │  :8083  (PostgreSQL+RabbitMQ)  │  │
│  └──────────┘  └───────────┘  └────────────────────────────────┘  │
│       ▲                                    │         │              │
│       └────────────────────────────────────┘         │              │
│                                                       │              │
│  ┌──────────┐  ┌───────────┐  ┌────────────────────────────────┐  │
│  │ MongoDB  │  │  Mongo    │  │         reservas-svc           │  │
│  │ :27017   │◄─┤ Express   │  │  :8082   (MongoDB+RabbitMQ)    │  │
│  └──────────┘  │  :6091    │  └────────────────────────────────┘  │
│                └───────────┘               │         │              │
│                                            └─────────┘              │
└──────────────────────────────────────────────────────────────────────┘
```

## 📁 Estructura de Archivos

```
tp-dan-2025/
├── docker-compose.yml           # ⚙️ Configuración base
├── docker-compose.override.yml  # 🛠️ Overrides para desarrollo (se aplica automático)
├── docker-compose.prod.yml      # 🏭 Configuración de producción
├── .env                         # 🔧 Variables de entorno (desarrollo)
├── .env.prod.template           # 📝 Template para producción
├── services/
│   ├── user-svc/
│   │   ├── Dockerfile               # 🐋 Multi-stage build (contexto raíz)
│   │   └── src/main/resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── application-local.properties
│   └── gestion-svc/
│       ├── Dockerfile               # 🐋 Multi-stage build multi-módulo (-am)
│       └── src/main/resources/
│           ├── application.properties
│           ├── application-dev.properties
│           ├── application-prod.properties
│           └── application-local.properties
│   └── reservas-svc/
│       ├── Dockerfile               # 🐋 Multi-stage build multi-módulo (-am)
│       └── src/main/resources/
│           ├── application.properties
│           ├── application-dev.properties
│           ├── application-prod.properties
│           └── application-local.properties
└── infra/
    └── mysql/
        └── initdb/              # 📊 Scripts de inicialización DB
```

## 🔧 Comandos Útiles

### Gestión de Servicios

```bash
# Detener todos los servicios
docker compose down

# Detener y eliminar volúmenes (⚠️ BORRA DATOS)
docker compose down -v

# Reiniciar un servicio específico
docker compose restart user-svc

# Reconstruir un servicio
docker compose up -d --build --no-deps user-svc

# Ver estado de los servicios
docker compose ps

# Ver recursos utilizados
docker stats
```

### Logs y Debugging

```bash
# Ver logs de todos los servicios
docker compose logs -f

# Ver logs desde hace 5 minutos
docker compose logs --since 5m

# Ver últimas 100 líneas
docker compose logs --tail 100 user-svc

# Entrar a un contenedor
docker compose exec user-svc sh
docker compose exec mysql bash

# Ver variables de entorno
docker compose exec user-svc env
```

### Base de Datos

```bash
# Conectarse a MySQL desde línea de comandos
docker compose exec mysql mysql -u usr_app -pusrapp users

# Backup de la base de datos
docker compose exec mysql mysqldump -u usr_app -pusrapp users > backup.sql

# Restaurar backup
docker compose exec -T mysql mysql -u usr_app -pusrapp users < backup.sql

# Ver tablas
docker compose exec mysql mysql -u usr_app -pusrapp -e "SHOW TABLES;" users
```

## 🔍 Troubleshooting

### Error: "Port already allocated"

```bash
# Ver qué proceso usa el puerto
netstat -ano | findstr :8081  # Windows
lsof -i :8081                 # Linux/Mac

# Cambiar puerto en docker-compose.override.yml
ports:
  - "8082:8080"  # Usar 8082 en lugar de 8081
```

### Error: "Cannot connect to MySQL"

```bash
# Verificar que MySQL esté healthy
docker compose ps

# Ver logs de MySQL
docker compose logs mysql

# Esperar a que esté listo
docker compose exec mysql mysqladmin ping -h localhost -u root -prootpwd
```

### Limpiar todo y empezar de cero

```bash
# Detener y eliminar TODO (contenedores, volúmenes, redes)
docker compose down -v

# Eliminar imágenes huérfanas
docker image prune -a

# Volver a construir
docker compose up -d --build
```

## 📊 Profiles de Spring Boot

### Development (`dev`)
- ✅ Swagger UI habilitado
- ✅ Logs verbosos (SQL visible)
- ✅ Debug remoto en puerto 5005
- ✅ Todos los endpoints actuator expuestos
- ✅ Stacktraces en errores

### Production (`prod`)
- 🔒 Swagger UI deshabilitado
- 🔒 Logs solo WARN/ERROR
- 🔒 Solo endpoints críticos de actuator
- 🔒 Sin stacktraces en errores
- 🔒 Pool de conexiones optimizado
- 🔒 Compresión HTTP habilitada

## 🔐 Seguridad

### Variables de Entorno

⚠️ **NUNCA** commitear archivos con credenciales reales:
- `.env.prod` debe estar en `.gitignore`
- Usar secretos/vault en producción real
- Cambiar contraseñas por defecto

### Best Practices Producción

1. **No exponer puertos innecesarios**: MySQL no debe ser accesible desde el host
2. **Usar HTTPS**: Implementar reverse proxy (nginx/traefik) con certificados SSL
3. **Limitar recursos**: Configurar `deploy.resources` para evitar consumo excesivo
4. **Healthchecks**: Monitorear estado de servicios
5. **Logs centralizados**: Usar ELK, Loki, o similar en producción real

## 📦 Volúmenes Persistentes

| Volumen | Descripción | Localización |
|---------|-------------|--------------|
| `mysql_data` | Base de datos MySQL | Docker volume |
| `postgres_data` | Base de datos PostgreSQL | Docker volume |
| `pgadmin_data` | Configuración PgAdmin | Docker volume |
| `./services/user-svc/logs` | Logs de user-svc | Host filesystem |
| `./services/gestion-svc/logs` | Logs de gestion-svc | Host filesystem |

```bash
# Ver información de volúmenes
docker volume ls
docker volume inspect tp-dan-mysql-data

# Backup de volumen
docker run --rm -v tp-dan-mysql-data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz -C /data .

# Restaurar volumen
docker run --rm -v tp-dan-mysql-data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql-backup.tar.gz -C /data
```

## 🧪 Testing

### Health Checks

```bash
# Health de user-svc
curl http://localhost:8081/actuator/health

# Health de MySQL
docker compose exec mysql mysqladmin ping -h localhost
```

### Endpoints Disponibles

```bash
# Info de la aplicación
curl http://localhost:8081/actuator/info

# Métricas
curl http://localhost:8081/actuator/metrics

# OpenAPI JSON
curl http://localhost:8081/openapi.json
```

## 📚 Etapas del Proyecto

### ✅ Etapa 1 (Actual)
- MySQL + PHPMyAdmin
- user-svc (microservicio de usuarios)
- Perfiles dev/prod
- Multi-stage Dockerfile

### 🔜 Etapa 2
- PostgreSQL
- gestion-svc
- reservas-svc
- RabbitMQ

### 🔜 Etapa 3
- MongoDB
- API Gateway
- Frontend React

## 🆘 Soporte

Para más información, consultar:
- [README principal](./README.md)
- [ETAPA01.md](./ETAPA01.md)
- [Docker Compose documentation](https://docs.docker.com/compose/)

---

**Desarrollado para TP DAN 2025 - UTN FRSF**

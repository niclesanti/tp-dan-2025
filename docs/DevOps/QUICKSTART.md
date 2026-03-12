# 🚀 Quick Start - Configuración Docker

## ✅ Lo que se ha creado

### 📁 Archivos de Docker Compose
- **docker-compose.yml** - Configuración base de servicios
- **docker-compose.override.yml** - Configuración de desarrollo (se aplica automáticamente)
- **docker-compose.prod.yml** - Configuración de producción

### 🔧 Archivos de Configuración
- **.env** - Variables de entorno para desarrollo (MySQL, PostgreSQL, RabbitMQ, PgAdmin)
- **.env.prod.template** - Template para configuración de producción
- **services/user-svc/Dockerfile** - Multi-stage build optimizado
- **services/user-svc/.dockerignore** - Optimización de contexto de build
- **services/gestion-svc/Dockerfile** - Multi-stage build con soporte multi-módulo (`-am`)

### 📝 Perfiles de Spring Boot
- **application-dev.properties** - Perfil de desarrollo (user-svc y gestion-svc)
- **application-prod.properties** - Perfil de producción (user-svc y gestion-svc)
- **application-local.properties** - Perfil para correr desde IDE (localhost)

### 📚 Documentación
- **DOCKER-README.md** - Guía completa de Docker
- **Makefile** - Comandos simplificados (opcional)

## 🎯 Inicio Inmediato

### Opción 1: Docker Compose (Recomendado)

```powershell
# Levantar servicios en modo desarrollo (compila automáticamente)
docker compose up -d --build

# Ver logs
docker compose logs -f

# Detener servicios
docker compose down
```

**Nota**: El `--build` compila automáticamente el código Java usando multi-stage build:
- **Primera vez**: ~70 segundos (descarga dependencias Maven)
- **Siguientes veces**: ~2 segundos (todo cacheado)
- **Al cambiar código**: ~15 segundos (solo recompila)

### Opción 2: Usando Makefile (Más cómodo)

```powershell
# Ver comandos disponibles
make help

# Levantar desarrollo
make dev-up

# Ver logs de user-svc
make logs-user

# Detener
make dev-down
```

## 🌐 Acceso a Servicios

Una vez levantados, accede a:

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **user-svc API** | http://localhost:8081 | Endpoints de gestión de usuarios |
| **user-svc Swagger** | http://localhost:8081/swagger-ui | Documentación interactiva user-svc |
| **gestion-svc API** | http://localhost:8083 | Endpoints de hoteles, habitaciones y tarifas |
| **gestion-svc Swagger** | http://localhost:8083/swagger-ui | Documentación interactiva gestion-svc |
| **PHPMyAdmin** | http://localhost:6080 | Administración MySQL |
| **PgAdmin** | http://localhost:6081 | Administración PostgreSQL |
| **RabbitMQ UI** | http://localhost:15672 | Consola de mensajería |

### Credenciales MySQL (Desarrollo)
- **Usuario**: `usr_app` / **Password**: `usrapp` / **BD**: `users`

### Credenciales PostgreSQL (Desarrollo)
- **Usuario**: `appuser` / **Password**: `apppwd` / **BD**: `appdb` / **Puerto host**: `5433`

### Credenciales RabbitMQ (Desarrollo)
- **Usuario**: `admin` / **Password**: `admin`

### Credenciales PgAdmin (Desarrollo)
- **Email**: `admin@admin.com` / **Password**: `admin`

## 📊 Verificar que todo funciona

```powershell
# Ver estado de todos los servicios
docker compose ps

# Verificar salud de user-svc
curl http://localhost:8081/actuator/health

# Verificar salud de gestion-svc (DB + RabbitMQ)
curl http://localhost:8083/actuator/health

# Ver logs en tiempo real
docker compose logs -f user-svc
docker compose logs -f gestion-svc
```

## 🔧 Características por Perfil

### Development (dev)
✅ Swagger UI habilitado  
✅ Debug remoto en puerto 5005  
✅ Logs verbosos con SQL  
✅ Todos los endpoints actuator  
✅ PHPMyAdmin accesible  

### Production (prod)
🔒 Swagger deshabilitado  
🔒 Sin debug remoto  
🔒 Logs optimizados (solo WARN/ERROR)  
🔒 Endpoints actuator limitados  
🔒 Límites de recursos configurados  

## 🏭 Para producción

```powershell
# 1. Crear configuración de producción
copy .env.prod.template .env.prod
# (Editar .env.prod con credenciales seguras)

# 2. Levantar en modo producción
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

## 🆘 Problemas Comunes

### Puerto 8081 ya está en uso
```powershell
# Ver qué proceso usa el puerto
netstat -ano | findstr :8081

# O cambiar el puerto en docker-compose.override.yml
# ports:
#   - "8082:8080"  # Usar otro puerto
```

### MySQL no arranca
```powershell
# Ver logs
docker compose logs mysql

# Limpiar volúmenes y reintentar
docker compose down -v
docker compose up -d --build
```

### Reconstruir desde cero
```powershell
# Eliminar todo y empezar de nuevo
docker compose down -v
docker system prune -f
docker compose up -d --build
```

## 📖 Más Información

- Ver [DOCKER-README.md](./DOCKER-README.md) para documentación completa
- Ver [README.md](./README.md) para información del proyecto
- Ver [ETAPA01.md](./ETAPA01.md) para requerimientos funcionales

## ✨ Próximos Pasos

1. ✅ Levantar los servicios: `docker compose up -d --build`
2. ✅ Verificar acceso a http://localhost:8081/swagger-ui (user-svc)
3. ✅ Verificar acceso a http://localhost:8083/swagger-ui (gestion-svc)
4. ✅ Probar con PHPMyAdmin: http://localhost:6080
5. ✅ Probar con PgAdmin: http://localhost:6081
6. ✅ Probar RabbitMQ Management UI: http://localhost:15672
7. ⬜ Implementar reservas-svc

---

💡 **Tip**: Si usas VSCode, instala la extensión "Docker" para gestionar contenedores visualmente.

🎓 **Proyecto**: TP DAN 2025 - Sistema de Gestión Hotelera  
📅 **Etapa**: 2 - user-svc + gestion-svc

# ╔════════════════════════════════════════════════════════════════════╗
# ║ Makefile para gestión simplificada del proyecto                  ║
# ║ TP DAN 2025 - Sistema de Gestión Hotelera                         ║
# ║                                                                    ║
# ║ Uso: make [comando]                                               ║
# ║ Ejemplo: make dev-up                                              ║
# ╚════════════════════════════════════════════════════════════════════╝

.PHONY: help dev-up dev-down prod-up prod-down logs status clean rebuild shell-user shell-mysql db-backup db-restore test test-observabilidad test-obs

# Colores para output
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
NC := \033[0m # No Color

## help: Muestra este mensaje de ayuda
help:
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  TP DAN 2025 - Comandos Disponibles$(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(YELLOW)Desarrollo:$(NC)"
	@echo "  make dev-up          - Levantar servicios en modo desarrollo"
	@echo "  make dev-down        - Detener servicios de desarrollo"
	@echo "  make dev-restart     - Reiniciar servicios de desarrollo"
	@echo ""
	@echo "$(YELLOW)Producción:$(NC)"
	@echo "  make prod-up         - Levantar servicios en modo producción"
	@echo "  make prod-down       - Detener servicios de producción"
	@echo ""
	@echo "$(YELLOW)Monitoreo:$(NC)"
	@echo "  make logs            - Ver logs de todos los servicios"
	@echo "  make logs-user       - Ver logs de user-svc"
	@echo "  make logs-mysql      - Ver logs de MySQL"
	@echo "  make status          - Ver estado de los servicios"
	@echo ""
	@echo "$(YELLOW)Mantenimiento:$(NC)"
	@echo "  make clean           - Limpiar contenedores y volúmenes"
	@echo "  make rebuild         - Reconstruir todas las imágenes"
	@echo "  make rebuild-user    - Reconstruir solo user-svc"
	@echo ""
	@echo "$(YELLOW)Base de Datos:$(NC)"
	@echo "  make db-backup       - Crear backup de MySQL"
	@echo "  make db-restore      - Restaurar backup de MySQL"
	@echo "  make db-shell        - Acceder a MySQL CLI"
	@echo ""
	@echo "$(YELLOW)Shell:$(NC)"
	@echo "  make shell-user      - Entrar al contenedor user-svc"
	@echo "  make shell-mysql     - Entrar al contenedor MySQL"
	@echo ""
	@echo "$(YELLOW)Testing:$(NC)"
	@echo "  make test            - Ejecutar tests unitarios"
	@echo "  make health          - Verificar health de servicios"
	@echo ""
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"

## dev-up: Levantar servicios en modo desarrollo
dev-up:
	@echo "$(GREEN)🚀 Levantando servicios en modo DESARROLLO...$(NC)"
	docker compose up -d --build
	@echo "$(GREEN)✅ Servicios levantados$(NC)"
	@echo "$(BLUE)📊 API REST: http://localhost:8081$(NC)"
	@echo "$(BLUE)📖 Swagger: http://localhost:8081/swagger-ui$(NC)"
	@echo "$(BLUE)🗄️  PHPMyAdmin: http://localhost:6080$(NC)"

## dev-down: Detener servicios de desarrollo
dev-down:
	@echo "$(YELLOW)🛑 Deteniendo servicios de desarrollo...$(NC)"
	docker compose down
	@echo "$(GREEN)✅ Servicios detenidos$(NC)"

## dev-restart: Reiniciar servicios de desarrollo
dev-restart: dev-down dev-up

## prod-up: Levantar servicios en modo producción
prod-up:
	@echo "$(GREEN)🚀 Levantando servicios en modo PRODUCCIÓN...$(NC)"
	docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --build
	@echo "$(GREEN)✅ Servicios levantados$(NC)"

## prod-down: Detener servicios de producción
prod-down:
	@echo "$(YELLOW)🛑 Deteniendo servicios de producción...$(NC)"
	docker compose -f docker-compose.yml -f docker-compose.prod.yml down
	@echo "$(GREEN)✅ Servicios detenidos$(NC)"

## logs: Ver logs de todos los servicios
logs:
	docker compose logs -f

## logs-user: Ver logs de user-svc
logs-user:
	docker compose logs -f user-svc

## logs-mysql: Ver logs de MySQL
logs-mysql:
	docker compose logs -f mysql

## status: Ver estado de los servicios
status:
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  Estado de los Servicios$(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@docker compose ps
	@echo ""
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  Uso de Recursos$(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@docker stats --no-stream

## clean: Limpiar contenedores y volúmenes
clean:
	@echo "$(RED)⚠️  ADVERTENCIA: Esto eliminará todos los datos$(NC)"
	@echo "$(YELLOW)Presiona Ctrl+C para cancelar...$(NC)"
	@sleep 3
	docker compose down -v
	docker system prune -f
	@echo "$(GREEN)✅ Limpieza completada$(NC)"

## rebuild: Reconstruir todas las imágenes
rebuild:
	@echo "$(GREEN)🔨 Reconstruyendo todas las imágenes...$(NC)"
	docker compose build --no-cache
	docker compose up -d
	@echo "$(GREEN)✅ Reconstrucción completada$(NC)"

## rebuild-user: Reconstruir solo user-svc
rebuild-user:
	@echo "$(GREEN)🔨 Reconstruyendo user-svc...$(NC)"
	docker compose build --no-cache user-svc
	docker compose up -d --no-deps user-svc
	@echo "$(GREEN)✅ user-svc reconstruido$(NC)"

## shell-user: Entrar al contenedor user-svc
shell-user:
	docker compose exec user-svc sh

## shell-mysql: Entrar al contenedor MySQL
shell-mysql:
	docker compose exec mysql bash

## db-shell: Acceder a MySQL CLI
db-shell:
	docker compose exec mysql mysql -u usr_app -pusrapp users

## db-backup: Crear backup de MySQL
db-backup:
	@echo "$(GREEN)💾 Creando backup de MySQL...$(NC)"
	@mkdir -p backups
	docker compose exec mysql mysqldump -u usr_app -pusrapp users > backups/backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "$(GREEN)✅ Backup creado en backups/$(NC)"

## db-restore: Restaurar backup de MySQL (uso: FILE=backup.sql)
db-restore:
	@if [ -z "$(FILE)" ]; then \
		echo "$(RED)❌ Error: Especifica el archivo con FILE=backup.sql$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)📥 Restaurando backup desde $(FILE)...$(NC)"
	docker compose exec -T mysql mysql -u usr_app -pusrapp users < $(FILE)
	@echo "$(GREEN)✅ Backup restaurado$(NC)"

## test: Ejecutar tests unitarios
test:
	@echo "$(GREEN)🧪 Ejecutando tests...$(NC)"
	cd services/user-svc && ./mvnw test
	@echo "$(GREEN)✅ Tests completados$(NC)"

## test-observabilidad: Ejecutar tests de observabilidad E2E
test-observabilidad:
	@echo "$(GREEN)🔭 Ejecutando tests de observabilidad...$(NC)"
	pwsh -NoProfile -ExecutionPolicy Bypass -File infra/test-observabilidad.ps1
	@echo "$(GREEN)✅ Tests de observabilidad completados$(NC)"

## test-obs: Alias rápido para test-observabilidad
test-obs: test-observabilidad

## health: Verificar health de servicios
health:
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  Health Check$(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(YELLOW)user-svc:$(NC)"
	@curl -sf http://localhost:8081/actuator/health | jq . || echo "$(RED)❌ No responde$(NC)"
	@echo ""
	@echo "$(YELLOW)MySQL:$(NC)"
	@docker compose exec mysql mysqladmin ping -h localhost -u root -prootpwd || echo "$(RED)❌ No responde$(NC)"
	@echo "$(GREEN)✅ Health check completado$(NC)"

.PHONY: help dev run build clean db-up db-down db-wait db-reset stop stop-app

DB_CONTAINER := food_notification_db_container
DB_USER      := notification_admin
DB_NAME      := food_notification_db
APP_PORT     := 8086
INFRA_DIR    := infra

help: ## Show available commands
	@echo "Food Notification Service"
	@echo ""
	@echo "Usage:"
	@echo "  make dev        Start DB + clean build + run app (one command)"
	@echo "  make run        Start DB + run app (no clean)"
	@echo "  make build      Clean compile"
	@echo "  make db-up      Start Postgres container"
	@echo "  make db-down    Stop Postgres container"
	@echo "  make db-reset   Reset Postgres volume (deletes all data)"
	@echo "  make stop       Stop app + Postgres"
	@echo "  make stop-app   Stop app only (port $(APP_PORT))"
	@echo ""
	@echo "URLs (when running):"
	@echo "  App:     http://localhost:$(APP_PORT)"
	@echo "  Health:  http://localhost:$(APP_PORT)/actuator/health"
	@echo "  Swagger: http://localhost:$(APP_PORT)/swagger-ui/index.html"

dev: db-up db-wait stop-app
	mvn clean spring-boot:run

run: db-up db-wait stop-app
	mvn spring-boot:run

build:
	mvn clean compile

clean:
	mvn clean

db-up:
	cd $(INFRA_DIR) && docker compose up -d

db-down:
	cd $(INFRA_DIR) && docker compose down

db-wait:
	@echo "Waiting for Postgres on port 5437..."
	@until docker exec $(DB_CONTAINER) pg_isready -U $(DB_USER) -d $(DB_NAME) >/dev/null 2>&1; do \
		sleep 1; \
	done
	@echo "Postgres is ready."

db-reset:
	cd $(INFRA_DIR) && docker compose down -v

stop-app:
	@-lsof -ti :$(APP_PORT) | xargs kill -9 2>/dev/null || true

stop: stop-app db-down

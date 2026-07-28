# food-notification-service

Notification bounded context for the Food Delivery Platform.

Consumes order, payment, and delivery events; pushes updates to clients via SSE and maintains an inbox (requirements 5.6.1 / 5.6.2).

**Phase 1** — Spring Boot scaffold, Postgres, Flyway baseline, actuator health, OpenAPI contract stub.

## Software requirements

| Software | Purpose |
|----------|---------|
| **Java 21** | Run the Spring Boot app |
| **Maven** | Build and run |
| **Docker Desktop** or **Rancher Desktop** | Postgres via Docker Compose |

> Docker must be **running** before `make dev`.

## Ports

| Component | Port |
|-----------|------|
| App | **8086** |
| Postgres | **5437** |

Kafka (`localhost:9092`) will be wired in Phase 2 — use delivery-service infra when testing consumers.

## Configuration

```bash
cp infra/.env.example infra/.env
```

Set `DB_PASSWORD` in `infra/.env` (must match `application.yaml`, default `notification_secure_pass_2026`).

`infra/.env` is gitignored.

## Start the application

```bash
cd food-notification-service
make dev
```

## URLs

| Resource | URL |
|----------|-----|
| Health | http://localhost:8086/actuator/health |
| Swagger UI | http://localhost:8086/swagger-ui/index.html |
| OpenAPI contract | `api-spec/notification-spec.yaml` |

## Makefile commands

| Command | Description |
|---------|-------------|
| `make dev` | Start Postgres, wait, clean build, run app |
| `make run` | Start Postgres and run (no clean) |
| `make build` | `mvn clean compile` |
| `make db-up` / `make db-down` | Postgres container |
| `make db-reset` | Drop Postgres volume |
| `make stop` | Stop app and Postgres |

## Verify build

```bash
mvn clean verify
```

## Roadmap

| Phase | Scope |
|-------|--------|
| 1 | Scaffold (this phase) |
| 2 | Kafka consumers + persistence |
| 3 | SSE streams (5.6.1) |
| 4 | Gateway routes + platform scripts |
| 5 | Inbox + alerts (5.6.2) |
| 6 | Tests + E2E extension |

# food-notification-service

Notification bounded context for the Food Delivery Platform.

Consumes order, payment, and delivery events; pushes updates to clients via SSE and maintains an inbox (requirements 5.6.1 / 5.6.2).

**Phase 1** — Spring Boot scaffold, Postgres, Flyway baseline, actuator health, OpenAPI contract stub.

**Phase 2** — Kafka consumers for `payment.events`, `order.events`, and `delivery.events`; persist notifications and `order_recipients` mapping (from payment events) for downstream delivery/order alerts.

**Phase 3** — SSE stream at `GET /notifications/stream` (5.6.1); gateway header auth; live push when Kafka events are persisted.

**Phase 4** — Gateway route `/notifications/**` → `:8086`; included in platform `build-all`, `run-all`, `verify-all`, `infra-up`.

**Phase 5** — Inbox API (`GET /notifications/inbox`) and mark-read (`PATCH /notifications/{id}/read`) for delivery/status alerts (5.6.2).

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

Kafka (`localhost:9092`) — start delivery-service infra before running consumers:

```bash
cd ../food-delivery-service && make db-up   # includes Kafka on :9092
```

Kafka UI: http://localhost:8088

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
| SSE stream | `GET /notifications/stream` (requires `X-User-Id` from gateway) |
| Inbox | `GET /notifications/inbox` |
| Mark read | `PATCH /notifications/{id}/read` |

### Inbox (via gateway)

```bash
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"e2e.customer@example.com","password":"E2ePass123!"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

curl -s "http://localhost:8090/notifications/inbox?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# Unread alerts only
curl -s "http://localhost:8090/notifications/inbox?unreadOnly=true" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# Mark as read
curl -s -X PATCH "http://localhost:8090/notifications/<notification-id>/read" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### SSE stream (via gateway)

```bash
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"e2e.customer@example.com","password":"E2ePass123!"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

curl -N http://localhost:8090/notifications/stream \
  -H "Accept: text/event-stream" \
  -H "Authorization: Bearer $TOKEN"
```

Direct service port (local debugging only):

```bash
curl -N http://localhost:8086/notifications/stream \
  -H "Accept: text/event-stream" \
  -H "X-User-Id: <customer-uuid>" \
  -H "X-User-Roles: CUSTOMER"
```

Events: `connected` on subscribe, then `notification` with JSON payload when Kafka events are processed.

## Platform integration

From the platform root:

```bash
make platform-build      # includes food-notification-service
make platform-infra-up   # Postgres :5437 + Kafka
make platform-run        # starts notification on :8086 before gateway
make platform-verify     # checks http://localhost:8086/actuator/health
```

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

## Kafka topics consumed

| Topic | Events handled |
|-------|----------------|
| `payment.events` | `PaymentConfirmed`, `PaymentFailed` |
| `order.events` | `OrderReadyForDelivery` |
| `delivery.events` | `DeliveryAssigned`, `DeliveryPickedUp`, `DeliveryInTransit`, `DeliveryDelivered` |

Payment events register `order_id → customer_id` in `order_recipients` so later order/delivery events can target the right inbox recipient.

## Roadmap

| Phase | Scope |
|-------|--------|
| 1 | Scaffold |
| 2 | Kafka consumers + persistence |
| 3 | SSE streams (5.6.1) |
| 4 | Gateway routes + platform scripts |
| 5 | Inbox + alerts (5.6.2) — this phase |
| 6 | Tests + E2E extension |

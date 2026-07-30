# food-notification-service

Notification bounded context for the Food Delivery Platform.

## Documentation

| Document | Description |
|----------|-------------|
| [docs/TDD.md](docs/TDD.md) | Technical Design Document |
| [docs/API.md](docs/API.md) | API endpoint index (3 operations) |
| [docs/endpoints/](docs/endpoints/) | Detailed endpoint reference |
| [api-spec/notification-spec.yaml](api-spec/notification-spec.yaml) | OpenAPI contract |

Platform-wide: [docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md), [docs/SECURITY.md](../docs/SECURITY.md).  
Gateway routes: [ROUTE-notification](../food-api-gateway/docs/endpoints/ROUTE-notification.md).

Consumes order, payment, and delivery events; pushes updates to clients via SSE and maintains an inbox (requirements 5.6.1 / 5.6.2).

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
| API (direct) | http://localhost:8086 |
| Health | http://localhost:8086/actuator/health |
| Swagger UI (direct, debugging) | http://localhost:8086/swagger-ui/index.html |
| OpenAPI contract | `api-spec/notification-spec.yaml` |
| OpenAPI JSON (direct) | http://localhost:8086/v3/api-docs |
| SSE stream | `GET /notifications/stream` (requires JWT via gateway) |
| Inbox | `GET /notifications/inbox` |
| Mark read | `PATCH /notifications/{id}/read` |

### Platform Swagger via gateway (recommended)

| Environment | Swagger UI |
|-------------|------------|
| Local (`make platform-dev`) | **http://localhost:8090/swagger-ui.html** |
| Kubernetes (ingress) | **http://localhost/swagger-ui.html** |

Notification inbox and stream endpoints are in the unified UI under **Notifications**. Use **Authorize** with a customer JWT.

Per-service OpenAPI via gateway: http://localhost:8090/openapi/notification/v3/api-docs

## Docker & Kubernetes

| Resource | Location |
|----------|----------|
| Container image | `Dockerfile` in this repo |
| CI | `.github/workflows/ci.yml` — `mvn verify` + GHCR image push on `master` |
| Full platform deploy | [food-platform-deploy](../food-platform-deploy/README.md) |

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
make platform-verify     # checks actuator health on all services
make e2e                 # full flow including notifications (5.6.1 / 5.6.2)
```

E2E via Kubernetes ingress: `GATEWAY_URL=http://localhost E2E_SKIP_VERIFY=true make e2e`

E2E notification checks (via gateway): SSE `connected`, inbox contains payment + delivery alert types for the order, mark-read succeeds. Tune waits with `E2E_NOTIFICATION_WAIT=25` if Kafka is cold.

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

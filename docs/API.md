# food-notification-service — API Index

## Base URLs

| Context | Base URL |
|---------|----------|
| **Via gateway (recommended)** | `http://localhost:8090` |
| **Direct service** | `http://localhost:8086` |

All `/notifications/**` paths require `Authorization: Bearer <JWT>`. Gateway injects `X-User-Id` (recipient) and `X-User-Roles`.

## Authentication summary

| Operation | Gateway role | Service rule |
|-----------|--------------|----------------|
| GET `/notifications/stream` | Any authenticated role | Stream scoped to `X-User-Id` |
| GET `/notifications/inbox` | Any authenticated role | Inbox for `X-User-Id` |
| PATCH `/notifications/{id}/read` | Any authenticated role | Must own notification |

Gateway: [ROUTE-notification](../../food-api-gateway/docs/endpoints/ROUTE-notification.md) (30 min SSE timeout).

## OpenAPI & Swagger

| Resource | URL |
|----------|-----|
| Spec | [notification-spec.yaml](../api-spec/notification-spec.yaml) |
| Swagger (direct) | http://localhost:8086/swagger-ui/index.html |
| Via gateway | http://localhost:8090/openapi/notification/v3/api-docs |
| Health | http://localhost:8086/actuator/health |

---

## Notifications

| Method | Path | Summary | Detail |
|--------|------|---------|--------|
| GET | `/notifications/stream` | SSE live notification stream | [Stream](./endpoints/GET-notifications-stream.md) |
| GET | `/notifications/inbox` | Paginated inbox | [Inbox](./endpoints/GET-notifications-inbox.md) |
| PATCH | `/notifications/{notificationId}/read` | Mark notification read | [Mark read](./endpoints/PATCH-notifications-notificationId-read.md) |

> Notifications are **created by Kafka consumers**, not via REST. See [TDD.md §5](./TDD.md#5-kafka-integration).

---

## Related

- [TDD.md](./TDD.md)
- [SECURITY.md](../../docs/SECURITY.md)
- Platform E2E: `scripts/e2e-test.sh`

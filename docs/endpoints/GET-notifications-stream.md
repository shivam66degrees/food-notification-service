# GET /notifications/stream

Open a **Server-Sent Events** stream for live notifications to the authenticated user.

## URLs

| Context | URL |
|---------|-----|
| Gateway | `GET http://localhost:8090/notifications/stream` |
| Direct | `GET http://localhost:8086/notifications/stream` |

## Auth

Bearer JWT — stream is scoped to `X-User-Id` from gateway.

## Request headers

| Header | Required | Notes |
|--------|----------|-------|
| `Authorization` | Yes (via gateway) | Bearer JWT |
| `Accept` | Recommended | `text/event-stream` |

Direct calls must include `X-User-Id` and optionally `X-User-Roles`.

## Response 200 OK

Content-Type: `text/event-stream`

### SSE event types

**1. `connected`** — sent immediately on subscribe:

```
event: connected
data: {"status":"connected"}
```

**2. `notification`** — when a Kafka event creates a notification for this user:

```
event: notification
data: {"id":"...","orderId":"...","eventType":"PaymentConfirmed","title":"Payment confirmed","body":"Your payment of 9.99 INR was confirmed.","createdAt":"2026-07-19T10:00:03Z"}
```

**3. Heartbeat** — comment every 30 seconds (keeps connection alive):

```
:heartbeat
```

## Connection limits

| Setting | Value |
|---------|-------|
| Emitter timeout | 30 minutes |
| Gateway route timeout | 30 minutes |

Reconnect after timeout or disconnect.

## Example

```bash
curl -N http://localhost:8090/notifications/stream \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: text/event-stream"
```

E2E customer: `e2e.customer@example.com` / `E2ePass123!`

## Side effects

None on subscribe — notifications arrive when other services publish Kafka events.

## Errors

| Status | When |
|--------|------|
| 401 | Missing `X-User-Id` |

## Related

- [TDD.md §6](../TDD.md#6-sse-stream)
- Gateway: [ROUTE-notification](../../../food-api-gateway/docs/endpoints/ROUTE-notification.md)

## OpenAPI

- **Operation ID:** `streamNotifications`

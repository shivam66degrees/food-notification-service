# GET /notifications/inbox

Paginated notification inbox for the authenticated user.

## URLs

| Context | URL |
|---------|-----|
| Gateway | `GET http://localhost:8090/notifications/inbox` |
| Direct | `GET http://localhost:8086/notifications/inbox` |

## Auth

Bearer JWT — inbox filtered to `X-User-Id`.

## Query parameters

| Param | Default | Notes |
|-------|---------|-------|
| `page` | `0` | Zero-based page index |
| `size` | `20` | Page size (max 100) |
| `unreadOnly` | `false` | If `true`, only unread items |

## Response 200 OK

```json
{
  "items": [
    {
      "id": "n1a2b3c4-e5f6-7890-abcd-ef1234567890",
      "orderId": "9ba70625-7d22-4f5a-95a7-dcabc0e2ad29",
      "eventType": "PaymentConfirmed",
      "title": "Payment confirmed",
      "body": "Your payment of 9.99 INR was confirmed.",
      "readAt": null,
      "createdAt": "2026-07-19T10:00:03Z"
    },
    {
      "id": "n1a2b3c4-e5f6-7890-abcd-ef1234567891",
      "orderId": "9ba70625-7d22-4f5a-95a7-dcabc0e2ad29",
      "eventType": "DeliveryDelivered",
      "title": "Delivered",
      "body": "Your order has been delivered. Enjoy your meal!",
      "readAt": "2026-07-19T10:20:00Z",
      "createdAt": "2026-07-19T10:18:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2
}
```

Sorted by `createdAt` descending.

## Example

```bash
# All notifications
curl -s "http://localhost:8090/notifications/inbox?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# Unread only
curl -s "http://localhost:8090/notifications/inbox?unreadOnly=true" \
  -H "Authorization: Bearer $TOKEN"
```

## Errors

| Status | When |
|--------|------|
| 401 | Not authenticated |

## OpenAPI

- **Operation ID:** `listInbox`

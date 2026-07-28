# PATCH /notifications/{notificationId}/read

Mark a notification as read for the authenticated user.

## URLs

| Context | URL |
|---------|-----|
| Gateway | `PATCH http://localhost:8090/notifications/{notificationId}/read` |
| Direct | `PATCH http://localhost:8086/notifications/{notificationId}/read` |

## Auth

Bearer JWT — notification must belong to `X-User-Id`.

## Request body

None.

## Response 200 OK

```json
{
  "id": "n1a2b3c4-e5f6-7890-abcd-ef1234567890",
  "orderId": "9ba70625-7d22-4f5a-95a7-dcabc0e2ad29",
  "eventType": "PaymentConfirmed",
  "title": "Payment confirmed",
  "body": "Your payment of 9.99 INR was confirmed.",
  "readAt": "2026-07-19T10:05:00Z",
  "createdAt": "2026-07-19T10:00:03Z"
}
```

Idempotent: calling again on already-read notification returns same item with existing `readAt`.

## Errors

| Status | When |
|--------|------|
| 404 | Notification not found or not owned by caller |
| 401 | Not authenticated |

## Example

```bash
curl -s -X PATCH "http://localhost:8090/notifications/$NOTIFICATION_ID/read" \
  -H "Authorization: Bearer $TOKEN"
```

## OpenAPI

- **Operation ID:** `markNotificationRead`

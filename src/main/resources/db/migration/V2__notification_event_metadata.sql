ALTER TABLE notifications
    ADD COLUMN order_id UUID,
    ADD COLUMN source_event_id UUID;

CREATE UNIQUE INDEX idx_notifications_source_event_id
    ON notifications (source_event_id)
    WHERE source_event_id IS NOT NULL;

CREATE TABLE order_recipients (
    order_id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

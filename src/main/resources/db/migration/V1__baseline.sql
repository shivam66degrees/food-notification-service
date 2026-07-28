-- Phase 1 baseline schema for notification bounded context (extended in later phases).

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_recipient_created
    ON notifications (recipient_user_id, created_at DESC);

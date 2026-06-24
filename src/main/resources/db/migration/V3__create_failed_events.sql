CREATE TABLE failed_events
(

    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic         VARCHAR(255),
    event_id      VARCHAR(255),
    payload       TEXT,
    error_message TEXT,
    retry_count   INTEGER DEFAULT 0,
    status        VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
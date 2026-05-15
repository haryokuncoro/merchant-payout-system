CREATE TABLE failed_events (

                               id UUID PRIMARY KEY,

                               topic VARCHAR(255),

                               event_id UUID,

                               payload TEXT,

                               error_message TEXT,

                               retry_count INTEGER DEFAULT 0,

                               status VARCHAR(50),

                               created_at TIMESTAMP NOT NULL
);
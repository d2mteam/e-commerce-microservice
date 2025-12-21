CREATE TABLE IF NOT EXISTS order_saga_tracker
(
    id                     bigserial PRIMARY KEY,
    order_id               uuid        NOT NULL,
    stage                  text        NOT NULL,
    user_id                uuid,
    expected_replies       int         NOT NULL,
    received_replies       int         NOT NULL DEFAULT 0,
    processed_products     jsonb       NOT NULL DEFAULT '[]'::jsonb,
    failure_reasons        jsonb       NOT NULL DEFAULT '[]'::jsonb,
    confirmation_reference uuid,
    finalized              boolean     NOT NULL DEFAULT false,
    version                bigint      NOT NULL DEFAULT 0,
    UNIQUE (order_id, stage)
);

CREATE INDEX IF NOT EXISTS idx_order_saga_tracker_order_stage
    ON order_saga_tracker (order_id, stage);

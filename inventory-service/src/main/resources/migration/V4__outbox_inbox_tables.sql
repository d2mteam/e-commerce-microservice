DROP TABLE IF EXISTS outbox_event;
DROP TABLE IF EXISTS inbox_event;

CREATE TABLE outbox_event
(
    id             bigserial PRIMARY KEY,
    aggregate_id   uuid        NOT NULL,
    event_type     text        NOT NULL,
    event_sequence int         NOT NULL,
    payload        jsonb       NOT NULL,
    created_at     timestamptz NOT NULL DEFAULT now(),
    sent_at        timestamptz,
    retry_count    int         NOT NULL DEFAULT 0,
    status         text        NOT NULL DEFAULT 'PENDING',
    -- PENDING | SENT | FAILED | RETRYING
    last_error     text
);

CREATE TABLE inbox_event
(
    id             bigserial PRIMARY KEY,
    aggregate_id   uuid        NOT NULL,
    event_sequence int         NOT NULL,
    payload        jsonb       NOT NULL,
    received_at    timestamptz NOT NULL DEFAULT now(),
    processed_at   timestamptz,
    retry_count    int         NOT NULL DEFAULT 0,
    status         text        NOT NULL DEFAULT 'RECEIVED',
    -- RECEIVED | PROCESSED | FAILED | RETRYING
    last_error     text
);

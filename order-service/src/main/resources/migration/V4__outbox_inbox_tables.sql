DROP TABLE IF EXISTS outbox_event;
DROP TABLE IF EXISTS inbox_event;

CREATE TABLE outbox_event
(
    id             bigserial PRIMARY KEY,
    aggregate_id   uuid        NOT NULL,
    event_type     text        NOT NULL,
    payload        jsonb       NOT NULL,
    retry_count    int         NOT NULL DEFAULT 0,
    status         text        NOT NULL DEFAULT 'PENDING'
    -- PENDING | SENT | FAILED | RETRYING
);

CREATE TABLE inbox_event
(
    id             bigserial PRIMARY KEY,
    aggregate_id   uuid        NOT NULL,
    payload        jsonb       NOT NULL,
    retry_count    int         NOT NULL DEFAULT 0,
    status         text        NOT NULL DEFAULT 'RECEIVED'
    -- RECEIVED | PROCESSED | FAILED | RETRYING
);

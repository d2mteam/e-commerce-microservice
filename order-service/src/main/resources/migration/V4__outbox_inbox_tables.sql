DROP TABLE IF EXISTS outbox_event;
DROP TABLE IF EXISTS inbound_event;

CREATE TABLE outbox_event
(
    id             bigserial PRIMARY KEY,
    aggregate_id   uuid        NOT NULL,
    aggregate_type text        NOT NULL,
    event_type     text        NOT NULL,
    event_version  int         NOT NULL,
    payload        jsonb       NOT NULL,
--     source_service text        NOT NULL,

    created_at     timestamptz NOT NULL DEFAULT now(),
    sent_at        timestamptz,
    retry_count    int         NOT NULL DEFAULT 0,
    status         text        NOT NULL DEFAULT 'PENDING',
    -- PENDING | SENT | FAILED | RETRYING
    last_error     text
);


CREATE TABLE inbound_event
(
    id             bigserial PRIMARY KEY,
    aggregate_id   uuid        NOT NULL,
    aggregate_type text        NOT NULL,
    event_type     text        NOT NULL,
    event_version  int         NOT NULL,
    payload        jsonb       NOT NULL,
--     source_service text        NOT NULL,
    received_at    timestamptz NOT NULL DEFAULT now(),
    processed_at   timestamptz,
    status         text        NOT NULL DEFAULT 'RECEIVED',
    -- RECEIVED | PROCESSED | FAILED
    last_error     text
);

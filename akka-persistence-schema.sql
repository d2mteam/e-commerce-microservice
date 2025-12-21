-- Akka Persistence JDBC schema for ByteArrayJournalDao / ByteArraySnapshotDao (version 4.0.0)
-- Apply separately on each service DB (order-service @ localhost:1000, inventory-service @ localhost:2001)

DROP TABLE IF EXISTS public.journal CASCADE;
DROP TABLE IF EXISTS public.snapshot CASCADE;

CREATE TABLE IF NOT EXISTS public.journal (
    ordering BIGSERIAL PRIMARY KEY,
    persistence_id VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    tags VARCHAR(255),
    message BYTEA NOT NULL,
    created BIGINT NOT NULL DEFAULT (CAST(extract(epoch FROM clock_timestamp()) * 1000 AS BIGINT))
);

CREATE UNIQUE INDEX IF NOT EXISTS journal_pid_seq_idx
    ON public.journal (persistence_id, sequence_number);

CREATE INDEX IF NOT EXISTS journal_ordering_idx
    ON public.journal (ordering);

CREATE TABLE IF NOT EXISTS public.snapshot (
    persistence_id VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    created BIGINT NOT NULL DEFAULT (CAST(extract(epoch FROM clock_timestamp()) * 1000 AS BIGINT)),
    snapshot BYTEA NOT NULL,
    PRIMARY KEY (persistence_id, sequence_number)
);

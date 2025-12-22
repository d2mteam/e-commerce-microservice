DROP TABLE IF EXISTS journal;

CREATE TABLE journal (
                         ordering BIGSERIAL,
                         persistence_id VARCHAR(255) NOT NULL,
                         sequence_number BIGINT NOT NULL,
                         deleted BOOLEAN DEFAULT FALSE NOT NULL,
                         tags VARCHAR(255),
                         message BYTEA NOT NULL,
                         created TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (persistence_id, sequence_number)
);

CREATE UNIQUE INDEX journal_ordering_idx ON journal(ordering);
CREATE INDEX journal_persistence_id_idx ON journal(persistence_id);

DROP TABLE IF EXISTS snapshot;

CREATE TABLE snapshot (
                          persistence_id VARCHAR(255) NOT NULL,
                          sequence_number BIGINT NOT NULL,
                          created TIMESTAMP WITH TIME ZONE NOT NULL,
                          snapshot BYTEA NOT NULL,
                          PRIMARY KEY (persistence_id, sequence_number)
);
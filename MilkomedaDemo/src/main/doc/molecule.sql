CREATE TABLE IF NOT EXISTS ES_AGGREGATE (
                                            ID              UUID     PRIMARY KEY,
                                            VERSION         INTEGER  NOT NULL,
                                            AGGREGATE_TYPE  TEXT     NOT NULL
);

CREATE INDEX IF NOT EXISTS IDX_ES_AGGREGATE_AGGREGATE_TYPE ON ES_AGGREGATE (AGGREGATE_TYPE);

CREATE TABLE IF NOT EXISTS ES_EVENT (
                                        ID              BIGSERIAL  PRIMARY KEY,
                                        TRANSACTION_ID  XID8       NOT NULL,
                                        AGGREGATE_ID    UUID       NOT NULL REFERENCES ES_AGGREGATE (ID),
                                        VERSION         INTEGER    NOT NULL,
                                        EVENT_TYPE      TEXT       NOT NULL,
                                        JSON_DATA       JSON       NOT NULL,
                                        UNIQUE (AGGREGATE_ID, VERSION)
);

CREATE INDEX IF NOT EXISTS IDX_ES_EVENT_TRANSACTION_ID_ID ON ES_EVENT (TRANSACTION_ID, ID);
CREATE INDEX IF NOT EXISTS IDX_ES_EVENT_AGGREGATE_ID ON ES_EVENT (AGGREGATE_ID);
CREATE INDEX IF NOT EXISTS IDX_ES_EVENT_VERSION ON ES_EVENT (VERSION);

CREATE TABLE IF NOT EXISTS ES_AGGREGATE_SNAPSHOT (
                                                     AGGREGATE_ID  UUID     NOT NULL REFERENCES ES_AGGREGATE (ID),
                                                     VERSION       INTEGER  NOT NULL,
                                                     JSON_DATA     JSON     NOT NULL,
                                                     PRIMARY KEY (AGGREGATE_ID, VERSION)
);

CREATE INDEX IF NOT EXISTS IDX_ES_AGGREGATE_SNAPSHOT_AGGREGATE_ID ON ES_AGGREGATE_SNAPSHOT (AGGREGATE_ID);
CREATE INDEX IF NOT EXISTS IDX_ES_AGGREGATE_SNAPSHOT_VERSION ON ES_AGGREGATE_SNAPSHOT (VERSION);

CREATE TABLE IF NOT EXISTS ES_EVENT_SUBSCRIPTION (
                                                     SUBSCRIPTION_NAME    TEXT    PRIMARY KEY,
                                                     LAST_TRANSACTION_ID  XID8    NOT NULL,
                                                     LAST_EVENT_ID        BIGINT  NOT NULL
);


CREATE OR REPLACE FUNCTION CHANNEL_EVENT_NOTIFY_FCT()
    RETURNS TRIGGER AS
$BODY$
DECLARE
    aggregate_type  TEXT;
BEGIN
    SELECT a.AGGREGATE_TYPE INTO aggregate_type FROM ES_AGGREGATE a WHERE a.ID = NEW.AGGREGATE_ID;
    PERFORM pg_notify('channel_event_notify', aggregate_type);
    RETURN NEW;
END;
$BODY$
    LANGUAGE PLPGSQL;

CREATE OR REPLACE TRIGGER CHANNEL_EVENT_NOTIFY_TRG
    AFTER INSERT ON ES_EVENT
    FOR EACH ROW
EXECUTE PROCEDURE CHANNEL_EVENT_NOTIFY_FCT();



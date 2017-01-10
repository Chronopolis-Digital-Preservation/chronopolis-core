-- repair, fulfillment, and associated tables
DROP TABLE IF EXISTS repair;
DROP SEQUENCE IF EXISTS repair_id_seq;
CREATE SEQUENCE repair_id_seq;
CREATE TABLE repair (
    id bigint PRIMARY KEY DEFAULT nextval('repair_id_seq'),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    status varchar(255),
    requester VARCHAR(255),
    bag_id BIGINT,
    fulfillment_id BIGINT
);

DROP TABLE IF EXISTS repair_file;
DROP SEQUENCE IF EXISTS repair_file_id_seq;
CREATE SEQUENCE repair_file_id_seq;
CREATE TABLE repair_file (
    id bigint PRIMARY KEY DEFAULT nextval('repair_file_id_seq'),
    path text,
    repair_id bigint
);

DROP TABLE IF EXISTS fulfillment;
DROP SEQUENCE IF EXISTS fulfillment_id_seq;
CREATE SEQUENCE fulfillment_id_seq;
CREATE TABLE fulfillment (
    id bigint PRIMARY KEY DEFAULT nextval('fulfillment_id_seq'),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    from_name VARCHAR(255),
    status VARCHAR(255),
    type VARCHAR(255),
    strategy_id BIGINT,
    repair_id BIGINT
);

DROP TABLE IF EXISTS strategy;
DROP SEQUENCE IF EXISTS strategy_id_seq;
CREATE SEQUENCE strategy_id_seq;
CREATE TABLE strategy (
    id bigint PRIMARY KEY DEFAULT nextval('strategy_id_seq'),
    api_key VARCHAR(255),
    url VARCHAR(255),
    link VARCHAR(255),
    type VARCHAR(255),
    fulfillment_id BIGINT
);

ALTER TABLE repair
    ADD CONSTRAINT FK_repair_bag FOREIGN KEY (bag_id) REFERENCES bag;

ALTER TABLE repair
    ADD CONSTRAINT FK_repair_ff FOREIGN KEY (fulfillment_id) REFERENCES fulfillment;

ALTER TABLE repair_file
    ADD CONSTRAINT FK_rf_repair FOREIGN KEY (repair_id) REFERENCES repair;

ALTER TABLE fulfillment
    ADD CONSTRAINT FK_ff_strat FOREIGN KEY (strategy_id) REFERENCES strategy;

ALTER TABLE fulfillment
    ADD CONSTRAINT FK_ff_repair FOREIGN KEY (repair_id) REFERENCES repair;

ALTER TABLE strategy
    ADD CONSTRAINT FK_strat_ff FOREIGN KEY (fulfillment_id) REFERENCES fulfillment;
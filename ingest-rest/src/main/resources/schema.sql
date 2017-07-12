-- HSQL Schema

DROP TABLE IF EXISTS users;
create table users (
   username varchar(256),
   password varchar(256),
   enabled boolean
);

DROP TABLE IF EXISTS authorities;
create table authorities (
  username varchar(256),
  authority varchar(256)
);

DROP TABLE IF EXISTS bag CASCADE;
CREATE TABLE bag (
  id bigint generated by DEFAULT as IDENTITY (start WITH 1),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  name VARCHAR(255),
  creator VARCHAR(255),
  depositor VARCHAR(255),
  location VARCHAR(255),
  token_location VARCHAR(255),
  token_digest VARCHAR(255),
  tag_manifest_digest VARCHAR(255),
  status VARCHAR(255),
  fixity_algorithm VARCHAR(255),
  size bigint not null,
  total_files bigint not null,
  required_replications int,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS node CASCADE;
create table node (
  id bigint generated by DEFAULT as IDENTITY (start WITH 1),
  enabled boolean,
  password VARCHAR(255),
  username VARCHAR(255),
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS replication;
CREATE TABLE replication (
  id bigint generated by DEFAULT as IDENTITY (start WITH 1),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  status VARCHAR (255),
  bag_link VARCHAR (255),
  token_link VARCHAR (255),
  protocol VARCHAR (255),
  received_tag_fixity VARCHAR (255),
  received_token_fixity VARCHAR (255),
  bag_id bigint,
  node_id bigint,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS restoration;
CREATE TABLE restoration (
  restoration_id bigint generated by DEFAULT as IDENTITY (start WITH 1),
  depositor VARCHAR (255),
  link VARCHAR (255),
  name VARCHAR (255),
  protocol VARCHAR (255),
  status VARCHAR (255),
  node_id bigint,
  PRIMARY KEY (restoration_id)
);

DROP TABLE IF EXISTS ace_token;
CREATE TABLE ace_token (
  id bigint generated by DEFAULT as IDENTITY (start WITH 1),
  create_date TIMESTAMP,
  filename VARCHAR (255),
  proof VARCHAR (1024),
  ims_service VARCHAR (255),
  algorithm VARCHAR (255),
  round bigint,
  bag bigint,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bag_distribution;
CREATE TABLE bag_distribution (
  id bigint generated by DEFAULT as IDENTITY (start WITH 1),
  bag_id bigint,
  node_id bigint,
  status VARCHAR (255),
  PRIMARY KEY (id)
);

ALTER TABLE replication
  ADD CONSTRAINT FK_repl_bag FOREIGN KEY (bag_id) REFERENCES bag;

ALTER TABLE replication
  ADD CONSTRAINT FK_repl_node FOREIGN KEY (node_id) REFERENCES node;

ALTER TABLE restoration
  ADD CONSTRAINT FK_rest_node FOREIGN KEY (node_id) REFERENCES node;

ALTER TABLE ace_token
  ADD CONSTRAINT FK_token_bag FOREIGN KEY (bag) REFERENCES bag;

ALTER TABLE bag_distribution
  ADD CONSTRAINT FK_bd_bag FOREIGN KEY (bag_id) REFERENCES bag;

ALTER TABLE bag_distribution
  ADD CONSTRAINT FK_bd_node FOREIGN KEY (node_id) REFERENCES node;

-- Repair API (1.5.0)
DROP TABLE IF EXISTS repair CASCADE;
CREATE TABLE repair (
    id bigint generated by DEFAULT as IDENTITY (start WITH 1),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    audit VARCHAR(255),
    status VARCHAR(255),
    requester VARCHAR(255), -- maybe should be a bigint for the node_id instead?
    to_node BIGINT,
    from_node BIGINT,
    bag_id BIGINT,
    cleaned BOOLEAN DEFAULT FALSE,
    replaced BOOLEAN DEFAULT FALSE,
    validated BOOLEAN DEFAULT FALSE,
    type VARCHAR(255),
    strategy_id BIGINT,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS repair_file;
CREATE TABLE repair_file (
    id bigint generated by DEFAULT as IDENTITY (start WITH 1),
    path VARCHAR(255),
    repair_id bigint,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS strategy;
CREATE TABLE strategy (
    id bigint generated by DEFAULT as IDENTITY (start WITH 1),
    api_key VARCHAR(255),
    url VARCHAR(255),
    link VARCHAR(255),
    type VARCHAR(255),
    PRIMARY KEY (id)
);

ALTER TABLE repair
    ADD CONSTRAINT FK_repair_bag FOREIGN KEY (bag_id) REFERENCES bag;

ALTER TABLE repair
    ADD CONSTRAINT FK_repair_to FOREIGN KEY (to_node) REFERENCES node;

ALTER TABLE repair
    ADD CONSTRAINT FK_repair_from FOREIGN KEY (from_node) REFERENCES node;

ALTER TABLE repair_file
    ADD CONSTRAINT FK_rf_repair FOREIGN KEY (repair_id) REFERENCES repair;

ALTER TABLE repair
    ADD CONSTRAINT FK_repair_strat FOREIGN KEY (strategy_id) REFERENCES strategy ON DELETE CASCADE;

DROP TABLE IF EXISTS storage_region;
CREATE TABLE storage_region (
    id BIGINT generated by DEFAULT as IDENTITY (start WITH 1),
    node_id BIGINT NOT NULL,
    data_type VARCHAR(255) NOT NULL,
    storage_type VARCHAR(255) NOT NULL,
    capacity BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY(id)
);

-- storage
-- note the size/total_files might still live in the bag, but are here for now as their usage emerges
DROP TABLE IF EXISTS storage;
CREATE TABLE storage (
    id BIGINT generated by DEFAULT as IDENTITY (start WITH 1),
    region_id BIGINT NOT NULL,
    active BOOLEAN,
    path VARCHAR(255),
    size BIGINT,
    total_files BIGINT,
    checksum VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY(id)
);

-- replication_config
DROP TABLE IF EXISTS replication_config;
CREATE TABLE replication_config(
    id BIGINT generated by DEFAULT as IDENTITY (start WITH 1),
    region_id BIGINT NOT NULL,
    server VARCHAR(255) NOT NULL,
    username VARCHAR(255), --nullable
    path VARCHAR(255),
    PRIMARY KEY(id)
);

-- FKs
ALTER TABLE storage_region
    ADD CONSTRAINT FK_sr_noded FOREIGN KEY (node_id) REFERENCES node;

ALTER TABLE storage
    ADD CONSTRAINT FK_storage_sr FOREIGN KEY (region_id) REFERENCES storage_region;

ALTER TABLE replication_config
    ADD CONSTRAINT FK_rc_sr FOREIGN KEY (region_id) REFERENCES storage_region;

CREATE TABLE IF NOT EXISTS users (
	username VARCHAR(64) PRIMARY KEY,
	password VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS string_num (
	id VARCHAR(64) PRIMARY KEY,
	num BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS autonum_string (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	str VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS string_string_versioned (
	id VARCHAR(64) PRIMARY KEY,
	str VARCHAR(64) NOT NULL,
	version BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS preprocess_entities (
	id VARCHAR(64) PRIMARY KEY,
	str VARCHAR(64) NOT NULL,
	last_updated BIGINT NOT NULL
);

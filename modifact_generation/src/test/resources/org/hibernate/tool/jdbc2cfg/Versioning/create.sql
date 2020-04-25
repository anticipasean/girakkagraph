CREATE TABLE WITH_VERSION (FIRST INT, SECOND INT, VERSION INT, NAME VARCHAR(256), PRIMARY KEY (FIRST))
CREATE TABLE NO_VERSION (FIRST INT, SECOND INT, NAME VARCHAR(256), PRIMARY KEY (SECOND))
CREATE TABLE WITH_REAL_TIMESTAMP (FIRST INT, SECOND INT, TIMESTAMP TIMESTAMP, NAME VARCHAR(256), PRIMARY KEY (FIRST))
CREATE TABLE WITH_FAKE_TIMESTAMP (FIRST INT, SECOND INT, TIMESTAMP INT, NAME VARCHAR(256), PRIMARY KEY (FIRST))

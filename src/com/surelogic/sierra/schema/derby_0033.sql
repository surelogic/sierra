CREATE TABLE SERVER_LOCATION_PROJECT (
  SERVER_ID BIGINT NOT NULL CONSTRAINT SLP_SERVER_FK REFERENCES SERVER_LOCATION (ID),
  PROJECT   VARCHAR(2000) NOT NULL,
  PRIMARY KEY (SERVER_ID,PROJECT)
)
<<>>
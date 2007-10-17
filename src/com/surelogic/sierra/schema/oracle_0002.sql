
CREATE TABLE SYNCH (
  PROJECT_ID        NUMBER NOT NULL CONSTRAINT SYNCH_PROJECT_FK REFERENCES PROJECT (ID),
  USER_ID           NUMBER NOT NULL CONSTRAINT SYNCH_USER_FK REFERENCES USER (ID),
  DATE_TIME         TIMESTAMP NOT NULL,
  COMMIT_REVISION   NUMBER NOT NULL,
  RECEIVED_REVISION NUMBER NOT NULL,
  PRIMARY KEY (PROJECT_ID,USER_ID,DATE_TIME)
);
<<>>
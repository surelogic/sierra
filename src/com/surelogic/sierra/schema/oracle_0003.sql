

--This was never used in schema 2
DROP TABLE SYNCH;
<<>>
CREATE TABLE SYNCH (
  PROJECT_ID        NUMBER NOT NULL CONSTRAINT SYNCH_PROJECT_FK REFERENCES PROJECT (ID),
  DATE_TIME         TIMESTAMP NOT NULL,
  COMMIT_REVISION   NUMBER NOT NULL,
  PRIOR_REVISION    NUMBER NOT NULL,
  PRIMARY KEY (PROJECT_ID,DATE_TIME)
)
<<>>
ALTER TABLE FINDINGS_OVERVIEW ADD COLUMN CATEGORY VARCHAR(32672)
<<>>
CREATE INDEX FO_CATEGORY_INDEX ON FINDINGS_OVERVIEW (CATEGORY)
<<>>
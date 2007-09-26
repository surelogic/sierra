
--
-- Eclipse Client Views
--
-- The Eclipse Client uses theses views for its presentation of results

CREATE VIEW PROJECT_OVERVIEW
  (PROJECT)
AS SELECT NAME FROM PROJECT;

-- Displays the latest scan for each project in the database
CREATE VIEW LATEST_SCANS
  (PROJECT,SCAN_ID,SCAN_UID,TIME)
AS SELECT 
   P.NAME "PROJECT", R.ID "SCAN_ID", R.UID, TIMES.TIME 
FROM SCAN R, PROJECT P,
   (
    SELECT MAX(R2.SCAN_DATE_TIME) AS TIME
    FROM
        SCAN R2,
        PROJECT P2
    WHERE
        P2.ID = R2.PROJECT_ID
    GROUP BY
        P2.NAME
   ) AS TIMES
WHERE R.SCAN_DATE_TIME = TIMES.TIME AND P.ID = R.PROJECT_ID;
--Displays the oldest scan for each project in the database, as long as it isn't also the newest scan.
CREATE VIEW OLDEST_SCANS
  (PROJECT,SCAN_ID,SCAN_UID,TIME)
AS SELECT 
   P.NAME "PROJECT", R.ID "SCAN_ID", R.UID, TIMES.TIME 
FROM SCAN R, PROJECT P,
   (
    SELECT MIN(R2.SCAN_DATE_TIME) AS TIME
    FROM
        SCAN R2,
        PROJECT P2
    WHERE
        P2.ID = R2.PROJECT_ID
    GROUP BY
        P2.NAME
   ) AS TIMES
WHERE R.SCAN_DATE_TIME = TIMES.TIME AND P.ID = R.PROJECT_ID AND R.ID NOT IN (SELECT SCAN_ID FROM LATEST_SCANS);

CREATE TABLE FINDING_TOOL_COUNT (
  FINDING_ID BIGINT UNIQUE NOT NULL CONSTRAINT FTC_FINDINGS_FK REFERENCES FINDING(ID),
  COUNT      INT    NOT NULL
);

CREATE TABLE FINDINGS_OVERVIEW (
  PROJECT_ID    BIGINT         NOT NULL CONSTRAINT OVERVIEW_PROJECT_FK REFERENCES PROJECT(ID),
  FINDING_ID    BIGINT         NOT NULL CONSTRAINT OVERVIEW_FINDING_FK REFERENCES FINDING(ID),
  EXAMINED      CHAR(1)        NOT NULL CONSTRAINT OVERVIEW_EXAMINED_CN CHECK (EXAMINED IN ('Y','N')),
  LAST_CHANGED  TIMESTAMP,
  IMPORTANCE    VARCHAR(10)    NOT NULL CONSTRAINT OVERVIEW_IMPORTANCE_CN CHECK (IMPORTANCE IN ('Irrelevant','Low','Medium','High','Critical')),
  STATE         CHAR(1)        NOT NULL CONSTRAINT OVERVIEW_STATE_CN CHECK (STATE IN ('N','F','U')),
  COMMENT_COUNT INT            NOT NULL,
  PROJECT       VARCHAR(255)   NOT NULL,
  PACKAGE       VARCHAR(32672) NOT NULL,
  CLASS         VARCHAR(32672) NOT NULL,
  FINDING_TYPE  VARCHAR(32672) NOT NULL,
  SUMMARY       VARCHAR(32672) NOT NULL
);

CREATE INDEX FO_EXAMINED ON FINDINGS_OVERVIEW (EXAMINED);
CREATE INDEX FO_FIXED ON FINDINGS_OVERVIEW (STATE);
CREATE INDEX FO_STATE ON FINDINGS_OVERVIEW (STATE);
CREATE INDEX FO_COMMENTS ON FINDINGS_OVERVIEW (COMMENT_COUNT);
CREATE INDEX FO_PROJECT ON FINDINGS_OVERVIEW (PROJECT);
CREATE INDEX FO_PACKAGE ON FINDINGS_OVERVIEW (PACKAGE);
CREATE INDEX FO_CLASS ON FINDINGS_OVERVIEW (CLASS);
CREATE INDEX FO_FINDING_TYPE ON FINDINGS_OVERVIEW (FINDING_TYPE);
CREATE INDEX FO_IMPORTANCE ON FINDINGS_OVERVIEW (IMPORTANCE);

CREATE VIEW FIXED_FINDINGS 
   (ID)
AS 
   SELECT AFR.FINDING_ID FROM OLDEST_SCANS OS, ARTIFACT A, ARTIFACT_FINDING_RELTN AFR
   WHERE A.SCAN_ID = OS.SCAN_ID AND AFR.ARTIFACT_ID = A.ID
   EXCEPT
   SELECT AFR.FINDING_ID FROM LATEST_SCANS OS, ARTIFACT A, ARTIFACT_FINDING_RELTN AFR
   WHERE A.SCAN_ID = OS.SCAN_ID AND AFR.ARTIFACT_ID = A.ID;

CREATE VIEW RECENT_FINDINGS 
   (ID)
AS 
   SELECT AFR.FINDING_ID FROM LATEST_SCANS OS, ARTIFACT A, ARTIFACT_FINDING_RELTN AFR
   WHERE A.SCAN_ID = OS.SCAN_ID AND AFR.ARTIFACT_ID = A.ID
   EXCEPT
   SELECT AFR.FINDING_ID FROM OLDEST_SCANS OS, ARTIFACT A, ARTIFACT_FINDING_RELTN AFR
   WHERE A.SCAN_ID = OS.SCAN_ID AND AFR.ARTIFACT_ID = A.ID;



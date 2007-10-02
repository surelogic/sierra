
DROP TABLE FINDINGS_OVERVIEW;

-- An overview of all findings in the database, by qualifier
CREATE TABLE TIME_SERIES_OVERVIEW (
  QUALIFIER_ID   BIGINT         NOT NULL CONSTRAINT OVERVIEW_QUALIFIER_FK REFERENCES QUALIFIER (ID),
  FINDING_ID     BIGINT         NOT NULL CONSTRAINT OVERVIEW_FINDING_FK REFERENCES FINDING(ID),
  PROJECT_ID     BIGINT         NOT NULL CONSTRAINT OVERVIEW_PROJECT_FK REFERENCES PROJECT(ID),
  EXAMINED       VARCHAR(3)     NOT NULL CONSTRAINT OVERVIEW_EXAMINED_CN CHECK (EXAMINED IN ('Yes','No')), -- Indicates whether someone has marked this finding as read
  LAST_CHANGED   TIMESTAMP, -- The time the latest audit was applied to this finding
  IMPORTANCE     VARCHAR(10)    NOT NULL CONSTRAINT OVERVIEW_IMPORTANCE_CN CHECK (IMPORTANCE IN ('Irrelevant','Low','Medium','High','Critical')),
  STATUS         VARCHAR(9)     NOT NULL CONSTRAINT OVERVIEW_STATUS_CN CHECK (STATUS IN ('New','Fixed','Unchanged')),
  LINE_OF_CODE   INT,
  ARTIFACT_COUNT INT, -- The number of artifacts in the latest scan in this qualifier for this finding
  COMMENT_COUNT  INT            NOT NULL, -- The number of comments on this finding (Does not include changes to importance, or summary)
  PROJECT        VARCHAR(255)   NOT NULL,
  PACKAGE        VARCHAR(32672) NOT NULL,
  CLASS          VARCHAR(32672) NOT NULL,
  FINDING_TYPE   VARCHAR(32672) NOT NULL,
  TOOL           VARCHAR(255),
  SUMMARY        VARCHAR(32672) NOT NULL
);

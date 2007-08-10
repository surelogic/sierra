CREATE TABLE SIERRA_USER (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  USER_NAME      VARCHAR (255) UNIQUE NOT NULL,
  ROLE           VARCHAR(32672)
);

CREATE TABLE TOOL (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  NAME           VARCHAR (255) UNIQUE NOT NULL
);

CREATE TABLE PROJECT (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  NAME           VARCHAR(32672)  UNIQUE NOT NULL,
  VERSION        VARCHAR(32672),
  REVISION       BIGINT        NOT NULL
);

CREATE TABLE PRODUCT (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  NAME           VARCHAR(32672)  UNIQUE NOT NULL
);

CREATE TABLE PRODUCT_PROJECT_RELTN (
  PRODUCT_ID     BIGINT       NOT NULL CONSTRAINT PPR_PRODUCT_FK REFERENCES PRODUCT (ID),
  PROJECT_ID     BIGINT       NOT NULL CONSTRAINT PPR_PROJECT_FK REFERENCES PROJECT (ID),
  PRIMARY KEY (PRODUCT_ID,PROJECT_ID)
);

CREATE TABLE SOURCE_FOLDER (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  PROJECT_ID     BIGINT        NOT NULL CONSTRAINT SOURCE_FOLDER_PROJECT_FK REFERENCES PROJECT (ID),
  LOCATION       VARCHAR(32672)  NOT NULL
);

CREATE TABLE BIN_FOLDER (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  PROJECT_ID     BIGINT        NOT NULL CONSTRAINT BIN_FOLDER_PROJECT_FK REFERENCES PROJECT (ID),
  LOCATION       VARCHAR(32672)  NOT NULL
);

CREATE TABLE LIBRARY (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  PROJECT_ID     BIGINT        NOT NULL CONSTRAINT LIBRARY_PROJECT_FK REFERENCES PROJECT (ID),
  NAME           VARCHAR(32672)  NOT NULL,
  LOCATION       VARCHAR(32672)  NOT NULL
);

CREATE TABLE RUN (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  USER_ID        BIGINT        NOT NULL CONSTRAINT RUN_SPSUSER_FK REFERENCES SIERRA_USER (ID),
  PROJECT_ID     BIGINT        NOT NULL CONSTRAINT RUN_PROJECT_FK REFERENCES PROJECT (ID),
  JAVA_VERSION   VARCHAR(32672),
  JAVA_VENDOR    VARCHAR(32672),
  RUN_DATE_TIME  TIMESTAMP     NOT NULL,
  STATUS         VARCHAR(10)   NOT NULL CONSTRAINT RUN_STATUS_CN CHECK (STATUS IN ('INPROGRESS', 'FINISHED'))
);

CREATE TABLE RUN_TOOL_RELTN (
  RUN_ID         BIGINT        NOT NULL CONSTRAINT RUN_TOOL_RUN_FK REFERENCES RUN (ID),
  TOOL_ID        BIGINT        NOT NULL CONSTRAINT RUN_TOOL_TOOL_FK REFERENCES TOOL (ID),
  PRIMARY KEY (RUN_ID, TOOL_ID)
);

CREATE TABLE ERROR (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  RUN_ID         BIGINT        NOT NULL CONSTRAINT ERROR_RUN_FK REFERENCES RUN (ID),
  TOOL_ID        BIGINT        NOT NULL CONSTRAINT ERROR_TOOL_FK REFERENCES TOOL (ID),
  MESSAGE        VARCHAR(32672)  NOT NULL
);


CREATE TABLE TRAIL (
  ID                 BIGINT             NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  UID               CHAR(36)
);

CREATE TABLE AUDIT (
  ID                 BIGINT              NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  USER_ID            BIGINT              NOT NULL CONSTRAINT AUDIT_SPSUSER_FK REFERENCES SIERRA_USER (ID),
  TRAIL_ID           BIGINT              NOT NULL CONSTRAINT AUDIT_TRAIL_FK REFERENCES TRAIL (ID),
  DATE_TIME          TIMESTAMP           NOT NULL,
  VALUE              VARCHAR(32672)      NOT NULL,
  EVENT              VARCHAR(32672)      NOT NULL,
  CONSTRAINT AUDIT_CN UNIQUE (USER_ID,TRAIL_ID, DATE_TIME, VALUE, EVENT)
);

CREATE TABLE FINDING_TYPE (
  ID                    BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  TOOL_ID               BIGINT        NOT NULL CONSTRAINT FINDING_TYPE_TOOL_FK REFERENCES TOOL (ID),
  MNEMONIC              VARCHAR(255)  NOT NULL,
  MNEMONIC_DISPLAY      VARCHAR(255)  NOT NULL,
  CATEGORY              VARCHAR(32672),
  CATEGORY_DISPLAY      VARCHAR(32672),
  LINK                  VARCHAR(32672),
  INFO                  VARCHAR(32672),
  CONSTRAINT FINDING_TYPE_CN UNIQUE (TOOL_ID,MNEMONIC)
);

CREATE TABLE FINDING (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  TRAIL_ID       BIGINT        NOT NULL CONSTRAINT FINDING_TRAIL_FK REFERENCES TRAIL (ID),
  IMPORTANCE     INT
);

CREATE TABLE FINDING_FINDING_TYPE_RELTN (
  FINDING_ID             BIGINT          NOT NULL CONSTRAINT FFTR_FINDING_FK REFERENCES FINDING (ID),
  FINDING_TYPE_ID        BIGINT          NOT NULL CONSTRAINT FFTR_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),
  PRIMARY KEY (FINDING_ID, FINDING_TYPE_ID)
);

CREATE TABLE QUALIFIER (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  NAME           VARCHAR(32672) UNIQUE NOT NULL
);

INSERT INTO QUALIFIER (NAME) VALUES ('Default');

CREATE TABLE QUALIFIER_RUN_RELTN (
  QUALIFIER_ID   BIGINT        NOT NULL CONSTRAINT QUALIFIER_RUN_QUALIFIER_FK REFERENCES QUALIFIER (ID),
  RUN_ID         BIGINT        NOT NULL CONSTRAINT QUALIFIER_RUN_RUN_FK REFERENCES RUN (ID)
);

CREATE TABLE QUALIFIER_FINDING_RELTN (
  QUALIFIER_ID   BIGINT        NOT NULL CONSTRAINT QUALIFIER_FINDING_QUALIFIER_FK REFERENCES QUALIFIER (ID),
  FINDING_ID     BIGINT        NOT NULL CONSTRAINT QUALIFIER_FINDING_RUN_FK REFERENCES RUN (ID)
);

CREATE TABLE COMPILATION_UNIT (
  ID             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  PATH           VARCHAR(32672),
  PACKAGE_NAME   VARCHAR(32672)  NOT NULL,
  CLASS_NAME     VARCHAR(32672)  NOT NULL
); 

CREATE TABLE SOURCE_LOCATION (
  ID                     BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  COMPILATION_UNIT_ID    BIGINT          NOT NULL CONSTRAINT SOURCE_LOCATION_COMPILATION_UNIT_FK REFERENCES COMPILATION_UNIT (ID),
  HASH                   BIGINT,
  LINE_OF_CODE           INT             NOT NULL,
  END_LINE_OF_CODE       INT             NOT NULL,
  LOCATION_TYPE          VARCHAR(6)      CONSTRAINT FIELD_TYPE_CN CHECK (LOCATION_TYPE IN ('METHOD', 'CLASS', 'FIELD')),
  IDENTIFIER             VARCHAR(32672)
);
--NOTE: Line of Code and End Line of Code may not have values, but we represent this with 0 instead of null.
CREATE INDEX SOURCE_LOCATION_HASH_INDEX ON SOURCE_LOCATION (HASH);

--TODO LIMIT THE LENGTH OF INSTANCE HASH

CREATE TABLE ARTIFACT (
  ID                 BIGINT              NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  RUN_ID             BIGINT              NOT NULL CONSTRAINT ARTIFACT_RUN_FK REFERENCES RUN (ID),
  FINDING_TYPE_ID    BIGINT              NOT NULL CONSTRAINT ARTIFACT_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),
  FINDING_ID         BIGINT              CONSTRAINT ARTIFACT_FINDING_FK REFERENCES FINDING (ID),
  PRIMARY_SOURCE_LOCATION_ID  BIGINT     NOT NULL CONSTRAINT ARTIFACT_PRIMARY_SOURCE_LOCATION_FK REFERENCES SOURCE_LOCATION (ID),
  PRIORITY           INT,
  SEVERITY           INT,
  MESSAGE            VARCHAR(32672)
);

CREATE TABLE ARTIFACT_SOURCE_LOCATION_RELTN (
  ARTIFACT_ID        BIGINT              NOT NULL CONSTRAINT ASLR_ARTIFACT_FK REFERENCES ARTIFACT (ID),
  SOURCE_LOCATION_ID BIGINT              NOT NULL CONSTRAINT ASLR_SOURCE_LOCATION_FK REFERENCES SOURCE_LOCATION (ID),
  PRIMARY KEY (ARTIFACT_ID, SOURCE_LOCATION_ID)
);

CREATE TABLE SIERRA_MATCH (
  PROJECT_ID         BIGINT              NOT NULL CONSTRAINT MATCH_PROJECT_FK REFERENCES PROJECT (ID),
  HASH               BIGINT              NOT NULL,
  PACKAGE_NAME       VARCHAR(32672)      NOT NULL,
  CLASS_NAME         VARCHAR(32672)      NOT NULL,
  FINDING_TYPE_ID    BIGINT              NOT NULL CONSTRAINT MATCH_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),
  QUALIFIER_ID       BIGINT              NOT NULL CONSTRAINT MATCH_QUALIFIER_FK REFERENCES QUALIFIER (ID),
  FINDING_ID         BIGINT              CONSTRAINT MATCH_FINDING_FK REFERENCES FINDING (ID),
  TRAIL_ID           BIGINT              NOT NULL CONSTRAINT MATCH_TRAIL_FK REFERENCES TRAIL (ID),
  PRIMARY KEY (HASH, PACKAGE_NAME, CLASS_NAME, FINDING_TYPE_ID)
);
  
CREATE INDEX MATCH_HASH_INDEX ON SIERRA_MATCH (HASH);

--
-- Eclipse Client Views
--
-- The Eclipse Client uses theses views for its presentation of results

CREATE VIEW PROJECT_OVERVIEW
  (PROJECT)
AS SELECT NAME FROM PROJECT;

CREATE VIEW LATEST_RUNS
  (PROJECT,RUN_ID,TIME)
AS SELECT P.NAME "PROJECT", R.ID "RUN_ID", TIMES.TIME FROM RUN R, PROJECT P,
      (
       SELECT MAX(R2.RUN_DATE_TIME) AS TIME
       FROM
           RUN R2,
           PROJECT P2
       WHERE
           P2.ID = R2.PROJECT_ID
       GROUP BY
           P2.NAME
      ) AS TIMES
   WHERE R.RUN_DATE_TIME = TIMES.TIME AND P.ID = R.PROJECT_ID;

CREATE VIEW FINDINGS_OVERVIEW
   (PROJECT,PACKAGE_NAME,CLASS_NAME,LOC,SUMMARY,IMPORTANCE_CODE,IMPORTANCE,TOOL,CATEGORY,MNEMONIC,FINDING_ID)
AS SELECT
   LR.PROJECT, CU.PACKAGE_NAME, CU.CLASS_NAME, S.LINE_OF_CODE, A.MESSAGE,
   F.IMPORTANCE,
   case
     when F.IMPORTANCE=0 then 'Irrelevant'
     when F.IMPORTANCE=1 then 'Low'
     when F.IMPORTANCE=2 then 'Medium'
     when F.IMPORTANCE=3 then 'High'
     when F.IMPORTANCE=4 then 'Critical'
   end,
   T.NAME, FT.CATEGORY_DISPLAY, FT.MNEMONIC_DISPLAY,F.ID
FROM
   LATEST_RUNS LR,
   ARTIFACT A,
   FINDING F,
   FINDING_TYPE FT,
   TOOL T,
   SOURCE_LOCATION S,
   COMPILATION_UNIT CU
WHERE
   A.RUN_ID = LR.RUN_ID AND
   S.ID = A.PRIMARY_SOURCE_LOCATION_ID AND
   CU.ID = S.COMPILATION_UNIT_ID AND
   F.ID = A.FINDING_ID AND
   FT.ID = A.FINDING_TYPE_ID AND
   T.ID = FT.TOOL_ID;

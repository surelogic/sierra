CREATE TABLE SIERRA_USER (
  ID         BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  USER_NAME  VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE TOOL (
  ID       BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  NAME     VARCHAR(255) NOT NULL,
  VERSION  VARCHAR(255) NOT NULL,
  CONSTRAINT TOOL_CN UNIQUE (NAME,VERSION)
);

CREATE TABLE PROJECT (
  ID                 BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  NAME               VARCHAR(255) UNIQUE NOT NULL,
  SERVER_UID         CHAR(36),
  SETTINGS_REVISION  BIGINT       NOT NULL DEFAULT 0,       
  SETTINGS           CLOB
);

CREATE TABLE SCAN (
  ID              BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  USER_ID         BIGINT          CONSTRAINT SCAN_SPSUSER_FK REFERENCES SIERRA_USER(ID),
  PROJECT_ID      BIGINT          NOT NULL CONSTRAINT SCAN_PROJECT_FK REFERENCES PROJECT(ID),
  UID             CHAR(36)        UNIQUE NOT NULL,
  JAVA_VERSION    VARCHAR(32672),
  JAVA_VENDOR     VARCHAR(32672),
  SCAN_DATE_TIME  TIMESTAMP       NOT NULL,
  STATUS          VARCHAR(10)     NOT NULL CONSTRAINT SCAN_STATUS_CN CHECK (STATUS IN ('LOADING', 'FINISHED'))
);

CREATE TABLE SCAN_ERROR (
  ID       BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  SCAN_ID  BIGINT         NOT NULL CONSTRAINT ERROR_SCAN_FK REFERENCES SCAN(ID) ON DELETE CASCADE,
  TOOL_ID  BIGINT         NOT NULL CONSTRAINT ERROR_TOOL_FK REFERENCES TOOL(ID),
  MESSAGE  VARCHAR(32672) NOT NULL
);

CREATE TABLE FINDING (
  ID                    BIGINT   NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  PROJECT_ID            BIGINT   NOT NULL CONSTRAINT FINDING_PROJECT_FK REFERENCES PROJECT (ID),
  OBSOLETED_BY_ID       BIGINT   CONSTRAINT FINDING_OBSOLETED_FINDING_FK REFERENCES FINDING (ID),
  UID                   CHAR(36),
  IS_READ               CHAR(1)  NOT NULL CONSTRAINT FINDING_READ_CN CHECK (IS_READ IN ('Y','N')),
  IMPORTANCE            INT,
  OBSOLETED_BY_REVISION BIGINT
);

CREATE TABLE AUDIT (
  ID          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  FINDING_ID  BIGINT          NOT NULL CONSTRAINT AUDIT_FINDING_FK REFERENCES FINDING (ID) ON DELETE CASCADE,
  DATE_TIME   TIMESTAMP       NOT NULL,
  USER_ID     BIGINT          CONSTRAINT AUDIT_SPSUSER_FK REFERENCES SIERRA_USER (ID),
  EVENT       VARCHAR(32672)  NOT NULL CONSTRAINT EVENT_CN CHECK (EVENT IN ('COMMENT','IMPORTANCE','READ')),
  VALUE       VARCHAR(32672),
  REVISION    BIGINT
);

CREATE TABLE FINDING_TYPE (
  ID                    BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  TOOL_ID               BIGINT        NOT NULL CONSTRAINT FINDING_TYPE_TOOL_FK REFERENCES TOOL (ID),
  MNEMONIC              VARCHAR(255)  NOT NULL,
  MNEMONIC_DISPLAY      VARCHAR(255)  NOT NULL,
  CATEGORY              VARCHAR(32672),
  LINK                  VARCHAR(32672),
  INFO                  VARCHAR(32672),
  CONSTRAINT FINDING_TYPE_CN UNIQUE (TOOL_ID, MNEMONIC)
);

CREATE TABLE COMPILATION_UNIT (
  ID            BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  PATH          VARCHAR(32672) NOT NULL,
  PACKAGE_NAME  VARCHAR(32672) NOT NULL,
  CLASS_NAME    VARCHAR(32672) NOT NULL,
  COUNT         INT            NOT NULL DEFAULT 0,
  CONSTRAINT COMPILATION_UNIT_CN UNIQUE (PATH, PACKAGE_NAME, CLASS_NAME)
); 

CREATE TABLE SOURCE_LOCATION (
  ID                   BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  COMPILATION_UNIT_ID  BIGINT         NOT NULL CONSTRAINT SOURCE_LOCATION_COMPILATION_UNIT_FK REFERENCES COMPILATION_UNIT (ID),
  HASH                 BIGINT,
  LINE_OF_CODE         INT            NOT NULL, -- no value represented by 0 instead of null
  END_LINE_OF_CODE     INT            NOT NULL, -- no value represented by 0 instead of null
  LOCATION_TYPE        VARCHAR(6)     CONSTRAINT FIELD_TYPE_CN CHECK (LOCATION_TYPE IN ('METHOD', 'CLASS', 'FIELD')),
  IDENTIFIER           VARCHAR(32672)
);
CREATE INDEX SOURCE_LOCATION_HASH_INDEX ON SOURCE_LOCATION(HASH);

CREATE TABLE CLASS_METRIC (
  SCAN_ID              BIGINT NOT NULL CONSTRAINT CLASS_METRIC_SCAN_FK REFERENCES SCAN (ID) ON DELETE CASCADE,
  COMPILATION_UNIT_ID  BIGINT NOT NULL CONSTRAINT CLASS_METRIC_COMPILATION_UNIT_FK REFERENCES COMPILATION_UNIT (ID),
  LINES_OF_CODE        INT,
  PRIMARY KEY (SCAN_ID,COMPILATION_UNIT_ID)
);

CREATE TABLE ARTIFACT (
  ID                          BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  SCAN_ID                     BIGINT         NOT NULL CONSTRAINT ARTIFACT_SCAN_FK REFERENCES SCAN (ID) ON DELETE CASCADE,
  FINDING_TYPE_ID             BIGINT         NOT NULL CONSTRAINT ARTIFACT_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),
  PRIMARY_SOURCE_LOCATION_ID  BIGINT         NOT NULL CONSTRAINT ARTIFACT_PRIMARY_SOURCE_LOCATION_FK REFERENCES SOURCE_LOCATION (ID),
  PRIORITY                    INT,
  SEVERITY                    INT,
  MESSAGE                     VARCHAR(32672)
);
CREATE INDEX ARTIFACT_SCAN_INDEX ON ARTIFACT (SCAN_ID);

CREATE TABLE ARTIFACT_FINDING_RELTN (
  ARTIFACT_ID  BIGINT NOT NULL CONSTRAINT AFR_ARTIFACT_FK REFERENCES ARTIFACT(ID) ON DELETE CASCADE,
  FINDING_ID   BIGINT NOT NULL CONSTRAINT AFR_FINDING_FK REFERENCES FINDING(ID) ON DELETE CASCADE,
  PRIMARY KEY (ARTIFACT_ID, FINDING_ID)
);

CREATE TABLE ARTIFACT_SOURCE_LOCATION_RELTN (
  ARTIFACT_ID        BIGINT NOT NULL CONSTRAINT ASLR_ARTIFACT_FK REFERENCES ARTIFACT(ID) ON DELETE CASCADE,
  SOURCE_LOCATION_ID BIGINT NOT NULL CONSTRAINT ASLR_SOURCE_LOCATION_FK REFERENCES SOURCE_LOCATION(ID),
  PRIMARY KEY (ARTIFACT_ID, SOURCE_LOCATION_ID)
);

CREATE TABLE LOCATION_MATCH (
  PROJECT_ID       BIGINT         NOT NULL CONSTRAINT MATCH_PROJECT_FK REFERENCES PROJECT (ID),
  HASH             BIGINT         NOT NULL,
  PACKAGE_NAME     VARCHAR(32672) NOT NULL,
  CLASS_NAME       VARCHAR(32672) NOT NULL,
  FINDING_TYPE_ID  BIGINT         NOT NULL CONSTRAINT MATCH_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),
  FINDING_ID       BIGINT         NOT NULL CONSTRAINT MATCH_FINDING_FK REFERENCES FINDING (ID),
  REVISION         BIGINT,
  PRIMARY KEY (PROJECT_ID,HASH, PACKAGE_NAME, CLASS_NAME, FINDING_TYPE_ID)
);
CREATE INDEX MATCH_HASH_INDEX ON LOCATION_MATCH (HASH);

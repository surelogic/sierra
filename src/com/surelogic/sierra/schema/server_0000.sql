
CREATE TABLE PRODUCT (
  ID   BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  NAME VARCHAR(32672) UNIQUE NOT NULL
);

CREATE TABLE PRODUCT_PROJECT_RELTN (
  PRODUCT_ID   BIGINT       NOT NULL CONSTRAINT PPR_PRODUCT_FK REFERENCES PRODUCT(ID) ON DELETE CASCADE,
  PROJECT_NAME VARCHAR(255) NOT NULL,
  PRIMARY KEY (PRODUCT_ID,PROJECT_NAME)
);

CREATE TABLE QUALIFIER (
  ID   BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  NAME VARCHAR(32672) UNIQUE NOT NULL
);

CREATE TABLE QUALIFIER_SCAN_RELTN (
  QUALIFIER_ID BIGINT NOT NULL CONSTRAINT QUALIFIER_SCAN_QUALIFIER_FK REFERENCES QUALIFIER(ID),
  SCAN_ID      BIGINT NOT NULL CONSTRAINT QUALIFIER_SCAN_SCAN_FK REFERENCES SCAN(ID) ON DELETE CASCADE,
  PRIMARY KEY (QUALIFIER_ID,SCAN_ID)
);

CREATE TABLE REVISION (
  REVISION  BIGINT    NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  DATE_TIME TIMESTAMP NOT NULL         
);

CREATE TABLE SETTINGS (
  ID       BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  NAME     VARCHAR(255) UNIQUE NOT NULL,
  REVISION BIGINT       NOT NULL
);

CREATE TABLE SETTINGS_PROJECT_RELTN (
  SETTINGS_ID    BIGINT       NOT NULL CONSTRAINT PSR_SETTINGS_FK REFERENCES SETTINGS(ID) ON DELETE CASCADE,
  PROJECT_NAME   VARCHAR(255) UNIQUE NOT NULL,
  PRIMARY KEY (SETTINGS_ID,PROJECT_NAME)
);

ALTER TABLE PROJECT DROP COLUMN SETTINGS_REVISION;
DROP TABLE PROJECT_FILTERS;

CREATE TABLE SETTING_FILTERS (
  SETTINGS_ID     BIGINT  NOT NULL CONSTRAINT SETTINGS_SETTINGS_FK REFERENCES SETTINGS (ID),
  FINDING_TYPE_ID BIGINT  NOT NULL CONSTRAINT SETTINGS_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),
  FILTER_TYPE     INTEGER,
  DELTA           INTEGER,
  IMPORTANCE      INTEGER,
  FILTERED        CHAR(1) CONSTRAINT SETTINGS_FILTERED_FK CHECK (FILTERED IS NULL OR FILTERED IN ('Y','N')),
  PRIMARY KEY (SETTINGS_ID,FINDING_TYPE_ID)      
);

CREATE TABLE SERVER (
  UID CHAR(36) NOT NULL PRIMARY KEY 
);


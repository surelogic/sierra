

CREATE TABLE PRODUCT (
  ID   NUMBER         NOT NULL  PRIMARY KEY,
  NAME VARCHAR(2000) UNIQUE NOT NULL
)
<<>>

CREATE SEQUENCE PRODUCT_SEQ
START WITH 1
INCREMENT BY 1
NOMAXVALUE
<<>>

CREATE TRIGGER PRODUCT_INC
BEFORE INSERT ON PRODUCT FOR EACH ROW
BEGIN
  SELECT PRODUCT_SEQ.NEXTVAL
  INTO :NEW.ID
  FROM DUAL;
END;
<<>>
CREATE TABLE PRODUCT_PROJECT_RELTN (
  PRODUCT_ID   NUMBER       NOT NULL CONSTRAINT PPR_PRODUCT_FK REFERENCES PRODUCT(ID) ON DELETE CASCADE,
  PROJECT_NAME VARCHAR(255) NOT NULL,
  PRIMARY KEY (PRODUCT_ID,PROJECT_NAME)
)
<<>>

CREATE TABLE QUALIFIER (
  ID   NUMBER         NOT NULL  PRIMARY KEY, 
  NAME VARCHAR(2000) UNIQUE NOT NULL
)
<<>>

CREATE SEQUENCE QUALIFIER_SEQ
START WITH 1
INCREMENT BY 1
NOMAXVALUE
<<>>

CREATE TRIGGER QUALIFIER_INC
BEFORE INSERT ON QUALIFIER FOR EACH ROW
BEGIN
  SELECT QUALIFIER_SEQ.NEXTVAL
  INTO :NEW.ID
  FROM DUAL;
END;
<<>>
CREATE TABLE QUALIFIER_SCAN_RELTN (
  QUALIFIER_ID NUMBER NOT NULL CONSTRAINT QUALIFIER_SCAN_QUALIFIER_FK REFERENCES QUALIFIER(ID),
  SCAN_ID      NUMBER NOT NULL CONSTRAINT QUALIFIER_SCAN_SCAN_FK REFERENCES SCAN(ID) ON DELETE CASCADE,
  PRIMARY KEY (QUALIFIER_ID,SCAN_ID)
)
<<>>

CREATE TABLE REVISION (
  REVISION  NUMBER    NOT NULL  PRIMARY KEY,
  DATE_TIME TIMESTAMP NOT NULL         
)
<<>>
CREATE SEQUENCE REVISION_SEQ
START WITH 1
INCREMENT BY 1
NOMAXVALUE
<<>>
CREATE TRIGGER REVISION_INC
BEFORE INSERT ON REVISION FOR EACH ROW
BEGIN
  SELECT REVISION_SEQ.NEXTVAL
  INTO :NEW.REVISION
  FROM DUAL;
END;
<<>>

CREATE TABLE SETTINGS (
  ID       NUMBER       NOT NULL  PRIMARY KEY,
  NAME     VARCHAR(255) UNIQUE NOT NULL,
  REVISION NUMBER       NOT NULL
)
<<>>

CREATE SEQUENCE SETTINGS_SEQ
START WITH 1
INCREMENT BY 1
NOMAXVALUE
<<>>

CREATE TRIGGER SETTINGS_INC
BEFORE INSERT ON SETTINGS FOR EACH ROW
BEGIN
  SELECT SETTINGS_SEQ.NEXTVAL
  INTO :NEW.ID
  FROM DUAL;
END;
<<>>
CREATE TABLE SETTINGS_PROJECT_RELTN (
  SETTINGS_ID    NUMBER       NOT NULL CONSTRAINT PSR_SETTINGS_FK REFERENCES SETTINGS(ID) ON DELETE CASCADE,
  PROJECT_NAME   VARCHAR(255) NOT NULL,
  PRIMARY KEY (SETTINGS_ID,PROJECT_NAME)
)
<<>>

ALTER TABLE PROJECT DROP COLUMN SETTINGS_REVISION
<<>>

CREATE TABLE SETTING_FILTERS (
  SETTINGS_ID     NUMBER  NOT NULL CONSTRAINT SETTING_SETTINGS_FK REFERENCES SETTINGS (ID) ON DELETE CASCADE,
  FINDING_TYPE_ID NUMBER  NOT NULL CONSTRAINT SETTING_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),
  DELTA           INTEGER,
  IMPORTANCE      INTEGER,
  FILTERED        CHAR(1) CONSTRAINT SETTING_FILTERED_FK CHECK (FILTERED IS NULL OR FILTERED IN ('Y'))   
)
<<>>

CREATE TABLE SERVER (
  UUID CHAR(36) NOT NULL PRIMARY KEY 
)
<<>>

CREATE TABLE EMAIL (
  ID NUMBER NOT NULL  PRIMARY KEY,
  ADDRESS  VARCHAR(320)
)
<<>>

CREATE SEQUENCE EMAIL_SEQ
START WITH 1
INCREMENT BY 1
NOMAXVALUE
<<>>

CREATE TRIGGER EMAIL_INC
BEFORE INSERT ON EMAIL FOR EACH ROW
BEGIN
  SELECT EMAIL_SEQ.NEXTVAL
  INTO :NEW.ID
  FROM DUAL;
END;
<<>>
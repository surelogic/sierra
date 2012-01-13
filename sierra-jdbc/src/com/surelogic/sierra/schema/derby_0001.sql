CREATE TABLE EXTENSION (
       ID      BIGINT        PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       NAME    VARCHAR(1024) NOT NULL,
       VERSION VARCHAR(1024) NOT NULL,
       PATH    VARCHAR(1024) UNIQUE NOT NULL,
       UNIQUE (NAME,VERSION))
<<>>
CREATE TABLE EXTENSION_ARTIFACT_TYPE_RELTN (
       EXTENSION_ID     BIGINT NOT NULL CONSTRAINT EATR_EXTENSION_FK REFERENCES EXTENSION (ID) ON DELETE CASCADE,
       ARTIFACT_TYPE_ID BIGINT NOT NULL CONSTRAINT EATR_AT_FK REFERENCES ARTIFACT_TYPE (ID) ON DELETE CASCADE,
       PRIMARY KEY (EXTENSION_ID,ARTIFACT_TYPE_ID))
<<>>
CREATE TABLE EXTENSION_FINDING_TYPE_RELTN (
       EXTENSION_ID    BIGINT NOT NULL CONSTRAINT EFTR_EXTENSION_FK REFERENCES EXTENSION (ID) ON DELETE CASCADE,
       FINDING_TYPE_ID BIGINT NOT NULL CONSTRAINT EFTR_FT_FK REFERENCES FINDING_TYPE (ID) ON DELETE CASCADE,
       PRIMARY KEY (EXTENSION_ID,FINDING_TYPE_ID))
<<>>
CREATE TABLE SCAN_EXTENSION (
       SCAN_ID      BIGINT NOT NULL CONSTRAINT SE_SCAN_FK REFERENCES SCAN (ID) ON DELETE CASCADE,
       EXTENSION_ID BIGINT NOT NULL CONSTRAINT SE_EXTENSION_FK REFERENCES EXTENSION (ID) ON DELETE CASCADE,
       PRIMARY KEY (SCAN_ID,EXTENSION_ID))
<<>>
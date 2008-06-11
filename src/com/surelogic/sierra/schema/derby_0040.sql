
CREATE TABLE SCAN_FINDING_RELATION_OVERVIEW (
  SCAN_ID           BIGINT       NOT NULL CONSTRAINT SFRO_SCAN_FK REFERENCES SCAN (ID) ON DELETE CASCADE,
  PARENT_FINDING_ID BIGINT       NOT NULL CONSTRAINT SFRO_PARENT_FINDING_FK REFERENCES FINDING(ID) ON DELETE CASCADE,
  CHILD_FINDING_ID  BIGINT       NOT NULL CONSTRAINT SFRO_CHILD_FINDING_FK REFERENCES FINDING(ID) ON DELETE CASCADE,
  RELATION_TYPE     VARCHAR(255) NOT NULL,
  PRIMARY KEY (SCAN_ID,PARENT_FINDING_ID,CHILD_FINDING_ID,RELATION_TYPE)
)
<<>>
CREATE INDEX SFRO_RELATION_TYPE_IDX ON SCAN_FINDING_RELATION_OVERVIEW (RELATION_TYPE)
<<>>
CREATE TABLE FINDING_RELATION_OVERVIEW (
  PARENT_FINDING_ID BIGINT       NOT NULL CONSTRAINT FRO_PARENT_FINDING_FK REFERENCES FINDING(ID) ON DELETE CASCADE,
  CHILD_FINDING_ID  BIGINT       NOT NULL CONSTRAINT FRO_CHILD_FINDING_FK REFERENCES FINDING(ID) ON DELETE CASCADE,
  PROJECT_ID        BIGINT       NOT NULL CONSTRAINT FRO_PROJECT_FK REFERENCES PROJECT(ID),
  RELATION_TYPE     VARCHAR(255) NOT NULL,
  STATUS            VARCHAR(9)   NOT NULL CONSTRAINT FRO_STATUS_CN CHECK (STATUS IN ('New','Fixed','Unchanged')),
  PRIMARY KEY (PARENT_FINDING_ID,CHILD_FINDING_ID,RELATION_TYPE)
)
<<>>
CREATE INDEX FRO_RELATION_TYPE_IDX ON FINDING_RELATION_OVERVIEW (RELATION_TYPE)
<<>>
CREATE INDEX FRO_STATUS_IDX ON FINDING_RELATION_OVERVIEW (STATUS)
<<>>

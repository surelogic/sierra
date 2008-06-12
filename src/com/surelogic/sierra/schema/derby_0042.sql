
ALTER TABLE ARTIFACT ADD COLUMN ASSURANCE_TYPE CHAR(1) CONSTRAINT ARTIFACT_ASSURANCE_CN CHECK (ASSURANCE_TYPE IS NULL OR ASSURANCE_TYPE IN ('I','C'))
<<>>
ALTER TABLE FINDINGS_OVERVIEW ADD COLUMN ASSURANCE_TYPE CHAR(1) CONSTRAINT FINDINGS_OVERVIEW_ASSURANCE_CN CHECK (ASSURANCE_TYPE IS NULL OR ASSURANCE_TYPE IN ('I','C'))
<<>>
CREATE INDEX F_O_ASSURANCE_IDX ON FINDINGS_OVERVIEW (ASSURANCE_TYPE)
<<>>
ALTER TABLE SCAN_OVERVIEW ADD COLUMN ASSURANCE_TYPE CHAR(1) CONSTRAINT SCAN_OVERVIEW_ASSURANCE_CN CHECK (ASSURANCE_TYPE IS NULL OR ASSURANCE_TYPE IN ('I','C'))
<<>>

ALTER TABLE SETTING_FILTERS DROP CONSTRAINT SETTING_FILTERED_FK
<<>>
UPDATE SETTING_FILTERS SET FILTERED = 'N' WHERE FILTERED IS NULL
<<>>
ALTER TABLE SETTING_FILTERS ADD CONSTRAINT SETTING_FILTERED_FK CHECK (FILTERED IN ('Y','N'))
<<>>

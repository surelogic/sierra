DROP TABLE EMAIL
<<>>
RENAME COLUMN NOTIFICATION.EMAIL TO TO_EMAIL
<<>>
ALTER TABLE NOTIFICATION ADD COLUMN SMTP_HOST VARCHAR(2000)
<<>>
ALTER TABLE NOTIFICATION ADD COLUMN SMTP_PASS VARCHAR(2000)
<<>>
ALTER TABLE NOTIFICATION ADD COLUMN SMTP_USER VARCHAR(2000)
<<>>
ALTER TABLE NOTIFICATION ADD COLUMN FROM_EMAIL VARCHAR(320)
<<>>
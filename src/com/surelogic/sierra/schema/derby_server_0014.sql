CREATE TABLE SIERRA_GROUP (
  ID   BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  NAME VARCHAR(255) UNIQUE NOT NULL,
  INFO VARCHAR(32672)
)

<<>>

CREATE TABLE GROUP_USER_RELTN (
  GROUP_ID BIGINT NOT NULL CONSTRAINT G_U_R_GROUP_FK REFERENCES SIERRA_GROUP (ID) ON DELETE CASCADE,
  USER_ID  BIGINT NOT NULL CONSTRAINT G_U_R_USER_FK REFERENCES SIERRA_USER (ID) ON DELETE CASCADE,
  PRIMARY KEY (GROUP_ID, USER_ID)
) 	      		 
<<>>
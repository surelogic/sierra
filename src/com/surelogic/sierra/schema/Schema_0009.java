package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;

/**
 * The intent of this schema update is to copy over the server settings
 * structure to the client. In addition, uuid is added as a unique identifier to
 * settings, since name will no longer be unique on the client when two servers
 * have settings w/ the same name.
 * 
 * @author nathan
 * 
 */
public class Schema_0009 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		final DBType type = JDBCUtils.getDb(c);
		if (JDBCUtils.isServer(c)) {
			final Statement st = c.createStatement();
      try {
        if (type == DBType.DERBY) {
          st.execute("ALTER TABLE SETTINGS ADD COLUMN UUID CHAR(36)");
        } else {
          st.execute("ALTER TABLE SETTINGS ADD UUID CHAR(36)");
        }
        final ResultSet set = st.executeQuery("SELECT ID FROM SETTINGS");
        try {
          final PreparedStatement updateSt = c
          .prepareStatement("UPDATE SETTINGS SET UUID = ? WHERE ID = ?");
          while (set.next()) {
            updateSt.setString(1, UUID.randomUUID().toString());
            updateSt.setLong(2, set.getLong(1));
            updateSt.execute();
          }
        } finally {
          set.close();
        }
        st.execute("ALTER TABLE SETTINGS ADD CONSTRAINT SETTINGS_UUID_CN CHECK (UUID IS NOT NULL)");
      } finally {
        st.close();
      }
		} else {
			Statement st = c.createStatement();
			// NOTE: Dropping this column drops some views as well. We will need
			// to recreate these.
			st.execute("ALTER TABLE PROJECT DROP COLUMN SERVER_UUID");
			if (type == DBType.DERBY) {
				st
						.execute("CREATE VIEW PROJECT_OVERVIEW (PROJECT) AS SELECT NAME FROM PROJECT");
				st
						.execute("CREATE VIEW LATEST_SCANS"
								+ "  (PROJECT,SCAN_ID,SCAN_UUID,TIME) "
								+ " AS SELECT "
								+ "   P.NAME \"PROJECT\", R.ID \"SCAN_ID\", R.UUID, TIMES.TIME "
								+ " FROM SCAN R, PROJECT P,"
								+ "   ("
								+ "    SELECT MAX(R2.SCAN_DATE_TIME) AS TIME"
								+ "    FROM"
								+ "        SCAN R2,"
								+ "        PROJECT P2"
								+ "    WHERE"
								+ "        P2.ID = R2.PROJECT_ID"
								+ "    GROUP BY"
								+ "        P2.NAME"
								+ "   ) AS TIMES"
								+ " WHERE R.SCAN_DATE_TIME = TIMES.TIME AND P.ID = R.PROJECT_ID");
				st
						.execute("CREATE VIEW OLDEST_SCANS"
								+ "  (PROJECT,SCAN_ID,SCAN_UUID,TIME) "
								+ " AS SELECT "
								+ "   P.NAME \"PROJECT\", R.ID \"SCAN_ID\", R.UUID, TIMES.TIME "
								+ " FROM SCAN R, PROJECT P,"
								+ "   ("
								+ "    SELECT MIN(R2.SCAN_DATE_TIME) AS TIME"
								+ "    FROM"
								+ "        SCAN R2,"
								+ "        PROJECT P2"
								+ "    WHERE"
								+ "        P2.ID = R2.PROJECT_ID"
								+ "    GROUP BY"
								+ "        P2.NAME"
								+ "   ) AS TIMES"
								+ " WHERE R.SCAN_DATE_TIME = TIMES.TIME AND P.ID = R.PROJECT_ID AND R.ID NOT IN (SELECT SCAN_ID FROM LATEST_SCANS)");
				st
						.execute("CREATE VIEW FIXED_FINDINGS "
								+ "   (ID) "
								+ " AS "
								+ "   SELECT SO.FINDING_ID FROM OLDEST_SCANS OS, SCAN_OVERVIEW SO"
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID"
								+ "   EXCEPT"
								+ "   SELECT SO.FINDING_ID FROM LATEST_SCANS OS, SCAN_OVERVIEW SO"
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID");
				st
						.execute("CREATE VIEW RECENT_FINDINGS "
								+ "   (ID) "
								+ " AS "
								+ "   SELECT SO.FINDING_ID FROM LATEST_SCANS OS, SCAN_OVERVIEW SO"
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID"
								+ "   EXCEPT"
								+ "   SELECT SO.FINDING_ID FROM OLDEST_SCANS OS, SCAN_OVERVIEW SO"
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID"

						);
				st
						.execute("CREATE VIEW CURRENT_FINDINGS"
								+ "   (ID) "
								+ "AS"
								+ "   SELECT SO.FINDING_ID FROM OLDEST_SCANS OS, SCAN_OVERVIEW SO"
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID"
								+ "   UNION"
								+ "   SELECT SO.FINDING_ID FROM LATEST_SCANS OS, SCAN_OVERVIEW SO"
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID");
				st
						.execute("CREATE TABLE SETTINGS ("
								+ "  ID       BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
								+ "  NAME     VARCHAR(255) NOT NULL,"
								+ "  UUID     CHAR(36)     UNIQUE NOT NULL,"
								+ "  REVISION BIGINT       NOT NULL" + ")");
				st
						.execute("CREATE TABLE SETTING_FILTER_SETS ("
								+ "SETTINGS_ID   BIGINT NOT NULL CONSTRAINT PFS_SETTING_FK REFERENCES SETTINGS (ID) ON DELETE CASCADE,"
								+ "FILTER_SET_ID BIGINT NOT NULL CONSTRAINT PFS_FILTER_SET_FK REFERENCES FILTER_SET (ID) ON DELETE CASCADE"
								+ ")");
				st
						.execute("CREATE TABLE SETTING_FILTERS ("
								+ "SETTINGS_ID     BIGINT  NOT NULL CONSTRAINT SETTING_SETTINGS_FK REFERENCES SETTINGS (ID) ON DELETE CASCADE,"
								+ "FINDING_TYPE_ID BIGINT  NOT NULL CONSTRAINT SETTING_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),"
								+ "DELTA           INTEGER,"
								+ "IMPORTANCE      INTEGER,"
								+ " FILTERED        CHAR(1) CONSTRAINT SETTING_FILTERED_FK CHECK (FILTERED IS NULL OR FILTERED IN ('Y'))"
								+ ")");
				st
						.execute("CREATE TABLE SETTINGS_PROJECT_RELTN ("
								+ "SETTINGS_ID    BIGINT       NOT NULL CONSTRAINT PSR_SETTINGS_FK REFERENCES SETTINGS(ID) ON DELETE CASCADE,"
								+ "PROJECT_NAME   VARCHAR(255) NOT NULL,"
								+ "PRIMARY KEY (SETTINGS_ID,PROJECT_NAME)"
								+ ")");
			} else {
				st.execute("CREATE VIEW PROJECT_OVERVIEW " + "  (PROJECT) "
						+ "AS SELECT NAME FROM PROJECT ");
				st
						.execute("CREATE VIEW LATEST_SCANS "
								+ "  (PROJECT,SCAN_ID,SCAN_UUID,TIME) "
								+ "AS SELECT  "
								+ "   P.NAME \"PROJECT\", R.ID \"SCAN_ID\", R.UUID, TIMES.TIME  "
								+ "FROM SCAN R, PROJECT P, "
								+ "   ( "
								+ "    SELECT MAX(R2.SCAN_DATE_TIME) AS TIME "
								+ "    FROM "
								+ "        SCAN R2, "
								+ "        PROJECT P2 "
								+ "    WHERE "
								+ "        P2.ID = R2.PROJECT_ID "
								+ "    GROUP BY "
								+ "        P2.NAME "
								+ "   ) TIMES "
								+ "WHERE R.SCAN_DATE_TIME = TIMES.TIME AND P.ID = R.PROJECT_ID ");
				st
						.execute("CREATE VIEW OLDEST_SCANS "
								+ "  (PROJECT,SCAN_ID,SCAN_UUID,TIME) "
								+ "AS SELECT  "
								+ "   P.NAME \"PROJECT\", R.ID \"SCAN_ID\", R.UUID, TIMES.TIME  "
								+ "FROM SCAN R, PROJECT P, "
								+ "   ( "
								+ "    SELECT MIN(R2.SCAN_DATE_TIME) AS TIME "
								+ "    FROM "
								+ "        SCAN R2, "
								+ "        PROJECT P2 "
								+ "    WHERE "
								+ "        P2.ID = R2.PROJECT_ID "
								+ "    GROUP BY "
								+ "        P2.NAME "
								+ "   ) TIMES "
								+ "WHERE R.SCAN_DATE_TIME = TIMES.TIME AND P.ID = R.PROJECT_ID AND R.ID NOT IN (SELECT SCAN_ID FROM LATEST_SCANS) ");
				st
						.execute("CREATE VIEW FIXED_FINDINGS  "
								+ "   (ID) "
								+ "AS  "
								+ "   SELECT SO.FINDING_ID FROM OLDEST_SCANS OS, SCAN_OVERVIEW SO "
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID "
								+ "   MINUS "
								+ "   SELECT SO.FINDING_ID FROM LATEST_SCANS OS, SCAN_OVERVIEW SO "
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID ");
				st
						.execute(

						"CREATE VIEW RECENT_FINDINGS  "
								+ "   (ID) "
								+ "AS  "
								+ "   SELECT SO.FINDING_ID FROM LATEST_SCANS OS, SCAN_OVERVIEW SO "
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID "
								+ "   MINUS "
								+ "   SELECT SO.FINDING_ID FROM OLDEST_SCANS OS, SCAN_OVERVIEW SO "
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID ");
				st
						.execute(

						"CREATE VIEW CURRENT_FINDINGS "
								+ "   (ID) "
								+ "AS "
								+ "   SELECT SO.FINDING_ID FROM OLDEST_SCANS OS, SCAN_OVERVIEW SO "
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID "
								+ "   UNION "
								+ "   SELECT SO.FINDING_ID FROM LATEST_SCANS OS, SCAN_OVERVIEW SO "
								+ "   WHERE SO.SCAN_ID = OS.SCAN_ID ");
				st
						.execute("CREATE TABLE SETTINGS ("
								+ "  ID       NUMBER       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
								+ "  NAME     VARCHAR(255) NOT NULL,"
								+ "  UUID     CHAR(36)     UNIQUE NOT NULL,"
								+ "  REVISION NUMBER       NOT NULL" + ")");
				st
						.execute("CREATE TABLE SETTING_FILTER_SETS ("
								+ "SETTINGS_ID   NUMBER NOT NULL CONSTRAINT PFS_SETTING_FK REFERENCES SETTINGS (ID) ON DELETE CASCADE,"
								+ "FILTER_SET_ID NUMBER NOT NULL CONSTRAINT PFS_FILTER_SET_FK REFERENCES FILTER_SET (ID) ON DELETE CASCADE"
								+ ")");
				st
						.execute("CREATE TABLE SETTING_FILTERS ("
								+ "SETTINGS_ID     NUMBER  NOT NULL CONSTRAINT SETTING_SETTINGS_FK REFERENCES SETTINGS (ID) ON DELETE CASCADE,"
								+ "FINDING_TYPE_ID NUMBER  NOT NULL CONSTRAINT SETTING_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),"
								+ "DELTA           INTEGER,"
								+ "IMPORTANCE      INTEGER,"
								+ " FILTERED        CHAR(1) CONSTRAINT SETTING_FILTERED_FK CHECK (FILTERED IS NULL OR FILTERED IN ('Y'))"
								+ ")");
				st
						.execute("CREATE TABLE SETTINGS_PROJECT_RELTN ("
								+ "SETTINGS_ID    NUMBER       NOT NULL CONSTRAINT PSR_SETTINGS_FK REFERENCES SETTINGS(ID) ON DELETE CASCADE,"
								+ "PROJECT_NAME   VARCHAR(255) NOT NULL,"
								+ "PRIMARY KEY (SETTINGS_ID,PROJECT_NAME)"
								+ ")");
			}
			st.execute("DELETE FROM PROJECT_FILTERS");
			st.execute("DROP TABLE PROJECT_FILTERS");
		}
		if (type == DBType.DERBY) {
			Statement st = c.createStatement();
			try {
			  st.execute("CREATE TABLE FILTER_SET_FILTERS ("
			      + "FILTER_SET_ID   BIGINT  NOT NULL CONSTRAINT F_S_F_FILTER_SET_FK REFERENCES FILTER_SET (ID) ON DELETE CASCADE,"
			      + "FINDING_TYPE_ID BIGINT  NOT NULL CONSTRAINT F_S_F_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),"
			      + "DELTA           INTEGER,"
			      + "IMPORTANCE      INTEGER,"
			      + " FILTERED       CHAR(1) CONSTRAINT F_S_F_FILTERED_FK CHECK (FILTERED IS NULL OR FILTERED IN ('Y'))"
			      + ")");
			} finally {
			  st.close();
			}
		} else {
			Statement st = c.createStatement();
      try {
			st
					.execute("CREATE TABLE FILTER_SET_FILTERS ("
							+ "FILTER_SET_ID   NUMBER  NOT NULL CONSTRAINT F_S_F_FILTER_SET_FK REFERENCES FILTER_SET (ID) ON DELETE CASCADE,"
							+ "FINDING_TYPE_ID NUMBER  NOT NULL CONSTRAINT F_S_F_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),"
							+ "DELTA           INTEGER,"
							+ "IMPORTANCE      INTEGER,"
							+ " FILTERED       CHAR(1) CONSTRAINT F_S_F_FILTERED_FK CHECK (FILTERED IS NULL OR FILTERED IN ('Y'))"
							+ ")");
      } finally {
        st.close();
      }
		}
		Statement st = c.createStatement();
    try {
		st
				.execute("CREATE INDEX S_F_FILTERED_INDEX ON SETTING_FILTERS (FILTERED)");
		st
				.execute("CREATE INDEX F_S_FILTERED_INDEX ON FILTER_SET_FILTERS (FILTERED)");
    } finally {
      st.close();
    }
	}
}

package com.surelogic.sierra.schema;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.MessageWarehouse;

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
			if (type == DBType.DERBY) {
				st.execute("ALTER TABLE SETTINGS ADD COLUMN UUID CHAR(36)");
			} else {
				st.execute("ALTER TABLE SETTINGS ADD UUID CHAR(36)");
			}
			final ResultSet set = st.executeQuery("SELECT ID FROM SETTINGS");
			final PreparedStatement updateSt = c
					.prepareStatement("UPDATE SETTINGS SET UUID = ? WHERE ID = ?");
			while (set.next()) {
				updateSt.setString(1, UUID.randomUUID().toString());
				updateSt.setLong(2, set.getLong(1));
			}
			st
					.execute("ALTER TABLE SETTINGS ADD CONSTRAINT SETTINGS_UUID_CN CHECK (UUID IS NOT NULL)");
		} else {
			Statement st = c.createStatement();
			if (type == DBType.DERBY) {
				st
						.execute("CREATE TABLE SETTINGS ("
								+ "  ID       BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
								+ "  NAME     VARCHAR(255) NOT NULL,"
								+ "  UUID     CHAR(36)     UNIQUE NOT NULL,"
								+ "  REVISION BIGINT       NOT NULL" + ")");
				st.execute("ALTER TABLE PROJECT DROP COLUMN SERVER_UUID");
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
				st
						.execute("CREATE TABLE SETTINGS ("
								+ "  ID       NUMBER       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
								+ "  NAME     VARCHAR(255) NOT NULL,"
								+ "  UUID     CHAR(36)     UNIQUE NOT NULL,"
								+ "  REVISION NUMBER       NOT NULL" + ")");
				st.execute("ALTER TABLE PROJECT DROP COLUMN SERVER_UUID");
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
			st
					.execute("CREATE TABLE FILTER_SET_FILTERS ("
							+ "FILTER_SET_ID   BIGINT  NOT NULL CONSTRAINT F_S_F_FILTER_SET_FK REFERENCES FILTER_SET (ID) ON DELETE CASCADE,"
							+ "FINDING_TYPE_ID BIGINT  NOT NULL CONSTRAINT F_S_F_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),"
							+ "DELTA           INTEGER,"
							+ "IMPORTANCE      INTEGER,"
							+ " FILTERED       CHAR(1) CONSTRAINT F_S_F_FILTERED_FK CHECK (FILTERED IS NULL OR FILTERED IN ('Y'))"
							+ ")");
		} else {
			Statement st = c.createStatement();
			st
					.execute("CREATE TABLE FILTER_SET_FILTERS ("
							+ "FILTER_SET_ID   NUMBER  NOT NULL CONSTRAINT F_S_F_FILTER_SET_FK REFERENCES FILTER_SET (ID) ON DELETE CASCADE,"
							+ "FINDING_TYPE_ID NUMBER  NOT NULL CONSTRAINT F_S_F_FINDING_TYPE_FK REFERENCES FINDING_TYPE (ID),"
							+ "DELTA           INTEGER,"
							+ "IMPORTANCE      INTEGER,"
							+ " FILTERED       CHAR(1) CONSTRAINT F_S_F_FILTERED_FK CHECK (FILTERED IS NULL OR FILTERED IN ('Y'))"
							+ ")");
		}
		Statement st = c.createStatement();
		st.execute("CREATE INDEX S_F_FILTERED_INDEX ON SETTING_FILTERS (FILTERED)");
		st.execute("CREATE INDEX F_S_FILTERED_INDEX ON FILTER_SET_FILTERS (FILTERED)");

		MessageWarehouse mw = MessageWarehouse.getInstance();
		FindingTypeManager ftMan = FindingTypeManager.getInstance(c);
		List<FindingTypes> types = new ArrayList<FindingTypes>(3);
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/findbugs.xml");
		types.add(mw.fetchFindingTypes(in));
		in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/pmd.xml");
		types.add(mw.fetchFindingTypes(in));
		in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/checkstyle.xml");
		types.add(mw.fetchFindingTypes(in));
		ftMan.updateFindingTypes(types, 0);
	}
}

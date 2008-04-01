package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs1_3_3.FindBugs1_3_3ToolInfoGenerator;
import com.surelogic.sierra.jdbc.JDBCUtils;

public class Schema_0025 implements SchemaAction {
	public void run(Connection conn) throws SQLException {
		// Alter synch table client-side only
		if (!JDBCUtils.isServer(conn)) {
			final Statement st = conn.createStatement();
			try {
				st.execute("ALTER TABLE SYNCH ADD COLUMN COMMIT_COUNT INTEGER");
				st.execute("ALTER TABLE SYNCH ADD COLUMN UPDATE_COUNT INTEGER");
			} finally {
				st.close();
			}
		}
		FindBugs1_3_3ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupFilters(conn);
	}
}

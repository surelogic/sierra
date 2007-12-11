package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs1_3_0.FindBugs1_3_0ToolInfoGenerator;

public class Schema_0006 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		FindBugs1_3_0ToolInfoGenerator.generateTool(conn);
		conn.commit();
	}
}

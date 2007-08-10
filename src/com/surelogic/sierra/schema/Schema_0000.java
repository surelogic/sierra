package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs1_2_1.FindBugs1_2_1ToolInfoGenerator;
import com.surelogic.sierra.pmd3_9.PMD3_9ToolInfoGenerator;

public class Schema_0000 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		PMD3_9ToolInfoGenerator.generateTool(conn);
		FindBugs1_2_1ToolInfoGenerator.generateTool(conn);
		conn.commit();
	}

}

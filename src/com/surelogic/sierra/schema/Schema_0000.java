package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs.FindBugsToolInfoGenerator;
import com.surelogic.sierra.pmd3_9.PMD3_9ToolInfoGenerator;

public class Schema_0000 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		PMD3_9ToolInfoGenerator.generateTool(conn);
		FindBugsToolInfoGenerator.generateTool(conn);
		conn.commit();
	}

}

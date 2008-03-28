package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs1_3_3.FindBugs1_3_3ToolInfoGenerator;

public class Schema_0025 implements SchemaAction {
	public void run(Connection conn) throws SQLException {
		FindBugs1_3_3ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupFilters(conn);
	}
}

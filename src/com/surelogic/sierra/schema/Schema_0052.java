package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs1_3_6.FindBugs1_3_6ToolInfoGenerator;

public class Schema_0052 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		FindBugs1_3_6ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupScanFilters(conn);
		SchemaUtil.setupCategories(conn);
	}
}

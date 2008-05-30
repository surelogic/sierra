package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jsure.JSure0_9ToolInfoGenerator;

public class Schema_0034 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		JSure0_9ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupScanFilters(conn);
		SchemaUtil.setupCategories(conn);
	}
}

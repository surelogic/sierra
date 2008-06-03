package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jsure.*;

public class Schema_0035 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		JSure1_0ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupScanFilters(conn);
		SchemaUtil.setupCategories(conn);
	}
}

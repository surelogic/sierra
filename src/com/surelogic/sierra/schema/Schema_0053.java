package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.pmd4_2_4.PMD4_2_4ToolInfoGenerator;

public class Schema_0053 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		PMD4_2_4ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupScanFilters(conn);
		SchemaUtil.setupCategories(conn);
	}
}

package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs1_3_4.FindBugs1_3_4ToolInfoGenerator;
import com.surelogic.sierra.pmd4_2_1.PMD4_2_1ToolInfoGenerator;

public class Schema_0030 implements SchemaAction {
	public void run(Connection conn) throws SQLException {
		PMD4_2_1ToolInfoGenerator.generateTool(conn);
		FindBugs1_3_4ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupScanFilters(conn);
	}
}

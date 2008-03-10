package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.pmd4_2.PMD4_2ToolInfoGenerator;

public class Schema_0023 implements SchemaAction {
	public void run(Connection conn) throws SQLException {
		PMD4_2ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupFilters(conn);
	}
}

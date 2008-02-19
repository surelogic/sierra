package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.cpd.CPD4_1ToolInfoGenerator;

public class Schema_0015 implements SchemaAction {
	public void run(Connection conn) throws SQLException {
		CPD4_1ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupFilters(conn);
	}
}

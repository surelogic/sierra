package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs1_3_1.FindBugs1_3_1ToolInfoGenerator;
import com.surelogic.sierra.pmd4_1.PMD4_1ToolInfoGenerator;

public class Schema_0012 implements SchemaAction {
	public void run(Connection conn) throws SQLException {
	  PMD4_1ToolInfoGenerator.generateTool(conn);
    FindBugs1_3_1ToolInfoGenerator.generateTool(conn);
    /* Moved to schema 14 (after adding CPD)
    SchemaUtil.updateFindingTypes(conn);
    SchemaUtil.setupFilters(conn);
    */
	}
}

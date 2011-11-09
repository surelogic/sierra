package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs1_3_9.FindBugs1_3_9ToolInfoGenerator;

public class Schema_0002 implements SchemaAction {

	public void run(final Connection conn) throws SQLException {
		FindBugs1_3_9ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupLocalScanFilter(conn);
		SchemaUtil.generateBuglinkCategories(conn);
	}

}

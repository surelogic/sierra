package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs2_0_0.*;
import com.surelogic.sierra.pmd4_3.PMD4_3ToolInfoGenerator;

public class Schema_0003 implements SchemaAction {

	@Override
  public void run(final Connection conn) throws SQLException {
		PMD4_3ToolInfoGenerator.generateTool(conn);
		FindBugs2_0_0ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupLocalScanFilter(conn);
		SchemaUtil.generateBuglinkCategories(conn);
	}

}

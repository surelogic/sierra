package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.cpd.CPD4_1ToolInfoGenerator;
import com.surelogic.sierra.findbugs1_3_6.FindBugs1_3_6ToolInfoGenerator;
import com.surelogic.sierra.jsure.JSure1_1ToolInfoGenerator;
import com.surelogic.sierra.pmd4_2_4.PMD4_2_4ToolInfoGenerator;

public class Schema_0000 implements SchemaAction {

	public void run(final Connection conn) throws SQLException {
		CPD4_1ToolInfoGenerator.generateTool(conn);
		JSure1_1ToolInfoGenerator.generateTool(conn);
		FindBugs1_3_6ToolInfoGenerator.generateTool(conn);
		PMD4_2_4ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupLocalScanFilter(conn);
		SchemaUtil.setupCategories(conn);
	}

}

package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.cpd.*;
import com.surelogic.sierra.pmd5_1_1.PMD5_1_1ToolInfoGenerator;

public class Schema_0005 implements SchemaAction {

    @Override
    public void run(Connection conn) throws SQLException {
		CPD5_1_1ToolInfoGenerator.generateTool(conn);
		PMD5_1_1ToolInfoGenerator.generateTool(conn);
		//FindBugs2_0_3ToolInfoGenerator.generateTool(conn);
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupLocalScanFilter(conn);
		SchemaUtil.generateBuglinkCategories(conn);
    }
}

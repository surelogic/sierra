package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.cpd.CPD5_2_3ToolInfoGenerator;
import com.surelogic.sierra.findbugs3_0_1.FindBugs3_0_1ToolInfoGenerator;
import com.surelogic.sierra.pmd5_2_3.PMD5_2_3ToolInfoGenerator;

public class Schema_0008 implements SchemaAction {

    @Override
    public void run(Connection conn) throws SQLException {
        CPD5_2_3ToolInfoGenerator.generateTool(conn);
        PMD5_2_3ToolInfoGenerator.generateTool(conn);
        FindBugs3_0_1ToolInfoGenerator.generateTool(conn);
        //SchemaUtil.updateFindingTypes(conn);
        //SchemaUtil.setupLocalScanFilter(conn);
        //SchemaUtil.setupCategories(conn);
        // SchemaUtil.generateBuglinkCategories(conn);
    }
}

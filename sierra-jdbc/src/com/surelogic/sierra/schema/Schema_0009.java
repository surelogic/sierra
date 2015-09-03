package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.cpd.CPD5_3_3ToolInfoGenerator;
import com.surelogic.sierra.pmd5_3_3.PMD5_3_3ToolInfoGenerator;

public class Schema_0009 implements SchemaAction {

    @Override
    public void run(Connection conn) throws SQLException {
        CPD5_3_3ToolInfoGenerator.generateTool(conn);
        PMD5_3_3ToolInfoGenerator.generateTool(conn);
        SchemaUtil.updateFindingTypes(conn);
        SchemaUtil.setupLocalScanFilter(conn);
        SchemaUtil.setupCategories(conn);
        // SchemaUtil.generateBuglinkCategories(conn);
    }
}

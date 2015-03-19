package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;

public class Schema_0006 implements SchemaAction {

    @Override
    public void run(Connection conn) throws SQLException {
//        SchemaUtil.updateFindingTypes(conn);
//        SchemaUtil.setupLocalScanFilter(conn);
//        SchemaUtil.setupCategories(conn);
        // SchemaUtil.generateBuglinkCategories(conn);
    }
}

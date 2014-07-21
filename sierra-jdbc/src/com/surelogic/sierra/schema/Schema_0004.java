package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.QB;
import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.cpd.CPD5_0_5ToolInfoGenerator;
import com.surelogic.sierra.findbugs2_0_3.FindBugs2_0_3ToolInfoGenerator;
import com.surelogic.sierra.pmd5_0_5.PMD5_0_5ToolInfoGenerator;

public class Schema_0004 implements SchemaAction {

    @Override
    public void run(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        try {
            String name = findName(st.executeQuery(QB
                    .get("SchemaUtil.findExtensionPathConstraint")));
            st.execute("ALTER TABLE EXTENSION DROP UNIQUE " + name);
        } finally {
            st.close();
        }

		CPD5_0_5ToolInfoGenerator.generateTool(conn);
		PMD5_0_5ToolInfoGenerator.generateTool(conn);
		FindBugs2_0_3ToolInfoGenerator.generateTool(conn);
		/*
		SchemaUtil.updateFindingTypes(conn);
		SchemaUtil.setupLocalScanFilter(conn);
		SchemaUtil.generateBuglinkCategories(conn);
		*/
    }

    String findName(ResultSet set) throws SQLException {
        try {
            while (set.next()) {
                String name = set.getString(1);
                String desc = set.getString(2);
                if (desc.equals("UNIQUE BTREE (4)")) {
                    return name;
                }
            }
        } finally {
            set.close();
        }
        throw new IllegalStateException(
                "Unique constraint not detected in EXTENSION (PATH)");

    }
}

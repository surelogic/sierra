package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.QB;
import com.surelogic.common.jdbc.SchemaAction;

public class Schema_0004 implements SchemaAction {

    @Override
    public void run(Connection c) throws SQLException {
        Statement st = c.createStatement();
        try {
            String name = findName(st.executeQuery(QB
                    .get("SchemaUtil.findExtensionPathConstraint")));
            st.execute("ALTER TABLE EXTENSION DROP UNIQUE " + name);
        } finally {
            st.close();
        }

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

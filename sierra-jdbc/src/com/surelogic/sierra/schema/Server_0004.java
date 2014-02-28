package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.SchemaAction;

public class Server_0004 implements SchemaAction {

    @Override
    public void run(Connection c) throws SQLException {
        final Statement st = c.createStatement();
        try {
            final ResultSet set = c.createStatement().executeQuery(
                    "SELECT UUID FROM SERVER");
            if (!set.next()) {
                throw new IllegalStateException(
                        "The server must have an assigned uuid");
            }
            final String serverUuid = set.getString(1);
            SchemaUtil.setupServerScanFilter(c, serverUuid);
        } finally {
            st.close();
        }
    }

}

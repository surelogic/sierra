package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.SchemaAction;

public class Server_0021 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		final Statement st = c.createStatement();
		try {
			st.execute("INSERT INTO QUALIFIER(NAME) VALUES('__ALL_SCANS__')");
		} finally {
			st.close();
		}
	}
}

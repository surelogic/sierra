package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.SchemaAction;

public class Schema_0026 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		final Statement st = c.createStatement();
		try {
			st.execute("DELETE FROM FILTER_ENTRY");
			st.execute("DELETE FROM FILTER_SET_RELTN");
			st.execute("DELETE FROM FILTER_SET");
		} finally {
			st.close();
		}
	}

}

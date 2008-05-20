package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;

public class Schema_0031 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		SchemaUtil.setupCategories(c);
	}

}

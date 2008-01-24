package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;

public class Schema_0011 implements SchemaAction {

	public void run(Connection c) throws SQLException {
	  /* Moved to schema 12 (after tool upgrade)
	  SchemaUtil.updateFindingTypes(c);
    SchemaUtil.setupFilters(c);
    */
	}
}

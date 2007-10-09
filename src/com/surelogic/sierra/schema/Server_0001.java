package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import com.surelogic.common.jdbc.SchemaAction;

public class Server_0001 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		c.createStatement().execute(
				"INSERT INTO SERVER (UUID) VALUES('"
						+ UUID.randomUUID().toString() + "')");

	}

}

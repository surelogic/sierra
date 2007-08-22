package com.surelogic.sierra.jdbc.run;

import java.sql.Connection;
import java.sql.SQLException;

public class RunManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private RunManager(Connection conn) throws SQLException {
		this.conn = conn;
	}

	public static RunManager getInstance(Connection conn) throws SQLException {
		return new RunManager(conn);
	}
}

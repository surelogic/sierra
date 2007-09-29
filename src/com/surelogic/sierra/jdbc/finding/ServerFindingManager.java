package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.SQLException;

public final class ServerFindingManager extends FindingManager {

	private ServerFindingManager(Connection conn) throws SQLException {
		super(conn);
	}

	public static ServerFindingManager getInstance(Connection conn)
			throws SQLException {
		return new ServerFindingManager(conn);
	}

}

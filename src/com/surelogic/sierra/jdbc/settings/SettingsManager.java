package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.SQLException;

public class SettingsManager {

	private final Connection conn;

	private SettingsManager(Connection conn) throws SQLException {
		this.conn = conn;
	}

	public static SettingsManager getInstance(Connection conn)
			throws SQLException {
		return new SettingsManager(conn);
	}
}

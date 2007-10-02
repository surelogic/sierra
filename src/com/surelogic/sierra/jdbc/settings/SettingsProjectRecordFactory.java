package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.SettingsProjectRecord;

public class SettingsProjectRecordFactory {
	@SuppressWarnings("unused")
	private final Connection conn;

	private final String INSERT = "INSERT INTO SETTINGS_PROJECT_RELTN (SETTINGS_ID, PROJECT_NAME) VALUES (?,?)";
	private final String DELETE = "DELETE FROM SETTINGS_PROJECT_RELTN WHERE SETTINGS_ID= ? AND PROJECT_NAME= ?";

	private final BaseMapper sprMapper;

	private SettingsProjectRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;

		sprMapper = new BaseMapper(conn, INSERT, null, DELETE);
	}

	public static SettingsProjectRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new SettingsProjectRecordFactory(conn);
	}

	public SettingsProjectRecord newSettingsProject() {
		return new SettingsProjectRecord(sprMapper);
	}
}

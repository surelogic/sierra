package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.SettingsProjectRecord;

public class SettingsRecordFactory {
	@SuppressWarnings("unused")
	private final Connection conn;

	private final String INSERT = "INSERT INTO SETTINGS_PROJECT_RELTN (SETTINGS_ID, PROJECT_NAME) VALUES (?,?)";
	private final String DELETE = "DELETE FROM SETTINGS_PROJECT_RELTN WHERE SETTINGS_ID= ? AND PROJECT_NAME= ?";

	private final RecordMapper sprMapper;

	private SettingsRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;
		sprMapper = new BaseMapper(conn, INSERT, null, DELETE, false);
	}

	public static SettingsRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new SettingsRecordFactory(conn);
	}

	public SettingsProjectRecord newSettingsProject() {
		return new SettingsProjectRecord(sprMapper);
	}

}

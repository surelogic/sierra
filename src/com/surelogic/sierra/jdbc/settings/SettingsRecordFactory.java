package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.FilterSetRecord;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.SettingsProjectRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public class SettingsRecordFactory {
	@SuppressWarnings("unused")
	private final Connection conn;

	private final String INSERT = "INSERT INTO SETTINGS_PROJECT_RELTN (SETTINGS_ID, PROJECT_NAME) VALUES (?,?)";
	private final String DELETE = "DELETE FROM SETTINGS_PROJECT_RELTN WHERE SETTINGS_ID= ? AND PROJECT_NAME= ?";

	private final RecordMapper sprMapper;
	private final UpdateRecordMapper filterSetMapper;
	private SettingsRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;
		sprMapper = new BaseMapper(conn, INSERT, null, DELETE, false);
		filterSetMapper = new UpdateBaseMapper(conn,
				"INSERT INTO FILTER_SET (UID,NAME,INFO) VALUES (?,?,?)",
				"SELECT ID,INFO FROM FILTER_SET WHERE NAME = ?",
				"DELETE FROM FILTER_SET WHERE ID = ?",
				"UPDATE FILTER_SET SET NAME = ?, INFO = ? WHERE ID = ?");
	}

	public static SettingsRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new SettingsRecordFactory(conn);
	}

	public SettingsProjectRecord newSettingsProject() {
		return new SettingsProjectRecord(sprMapper);
	}
	

	public FilterSetRecord newFilterSetRecord() {
		return new FilterSetRecord(filterSetMapper);
	}
}

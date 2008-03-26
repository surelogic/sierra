package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.FindingTypeFilterRecord;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.SettingsProjectRecord;
import com.surelogic.sierra.jdbc.record.SettingsRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public final class SettingsRecordFactory {
	@SuppressWarnings("unused")
	private final Connection conn;

	private final RecordMapper sprMapper;
	private final UpdateRecordMapper settingsMapper;
	private final BaseMapper settingsFilterMapper;
	private final BaseMapper filterSetFilterMapper;

	private SettingsRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;
		sprMapper = new BaseMapper(
				conn,
				"INSERT INTO SETTINGS_PROJECT_RELTN (SETTINGS_ID, PROJECT_NAME) VALUES (?,?)",
				null,
				"DELETE FROM SETTINGS_PROJECT_RELTN WHERE SETTINGS_ID= ? AND PROJECT_NAME= ?",
				false);
		settingsMapper = new UpdateBaseMapper(conn,
				"INSERT INTO SETTINGS (UUID, NAME, REVISION) VALUES (?,?,?)",
				"SELECT ID,NAME,REVISION FROM SETTINGS WHERE UUID = ?",
				"DELETE FROM SETTINGS WHERE ID = ?",
				"UPDATE SETTINGS SET REVISION = ?, NAME = ? WHERE ID = ?");
		settingsFilterMapper = new BaseMapper(
				conn,
				"INSERT INTO SETTING_FILTERS (SETTINGS_ID, FINDING_TYPE_ID,DELTA,IMPORTANCE,FILTERED) VALUES (?,?,?,?,?)",
				null, null, false);
		filterSetFilterMapper = new BaseMapper(
				conn,
				"INSERT INTO FILTER_SET_FILTERS (FILTER_SET_ID, FINDING_TYPE_ID,DELTA,IMPORTANCE,FILTERED) VALUES (?,?,?,?,?)",
				null, null, false);
	}

	public static SettingsRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new SettingsRecordFactory(conn);
	}

	public SettingsProjectRecord newSettingsProject() {
		return new SettingsProjectRecord(sprMapper);
	}

	public FindingTypeFilterRecord newSettingsFilterRecord() {
		return new FindingTypeFilterRecord(settingsFilterMapper);
	}

	public FindingTypeFilterRecord newFilterSetFilterRecord() {
		return new FindingTypeFilterRecord(filterSetFilterMapper);
	}

	public SettingsRecord newSettingsRecord() {
		return new SettingsRecord(settingsMapper);
	}

}

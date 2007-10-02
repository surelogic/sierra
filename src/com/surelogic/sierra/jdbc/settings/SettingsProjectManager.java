package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.RecordStringRelationRecord;
import com.surelogic.sierra.jdbc.record.SettingsProjectRecord;
import com.surelogic.sierra.jdbc.record.SettingsRecord;

public class SettingsProjectManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private static final String GET_PROJECT_NAMES = "SELECT PR.NAME FROM SETTINGS SS, SETTINGS_PROJECT_RELTN SPR, PROJECT PR WHERE SS.NAME = ? AND SPR.SETTINGS_ID = SS.ID AND PR.NAME = SPR.PROJECT_NAME";
	private final PreparedStatement getProjectNames;

	private final SettingsProjectRecordFactory sprFactory;

	private SettingsProjectManager(Connection conn) throws SQLException {
		this.conn = conn;

		sprFactory = SettingsProjectRecordFactory.getInstance(conn);

		getProjectNames = conn.prepareStatement(GET_PROJECT_NAMES);
	}

	public void addRelation(SettingsRecord settings, String projectName)
			throws SQLException {

		if (settings == null)
			throw new SQLException();

		SLLogger.getLogger().log(
				Level.FINE,
				"Setting: " + settings.getName() + " projectName: "
						+ projectName);

		SettingsProjectRecord spr = sprFactory.newSettingsProject();
		spr.setId(new RecordStringRelationRecord.PK<SettingsRecord, String>(
				settings, projectName));
		spr.insert();
	}

	public Collection<String> getProjectNames(String setting)
			throws SQLException {

		getProjectNames.setString(1, setting);
		ResultSet rs = getProjectNames.executeQuery();

		Collection<String> projectNames = new ArrayList<String>();
		while (rs.next()) {
			projectNames.add(rs.getString(1));
		}
		rs.close();
		return projectNames;
	}

	public void deleteProjectRelation(SettingsRecord settings,
			String projectName) throws SQLException {

		if (settings == null)
			throw new SQLException();

		SettingsProjectRecord spr = sprFactory.newSettingsProject();
		spr.setId(new RecordStringRelationRecord.PK<SettingsRecord, String>(
				settings, projectName));
		spr.delete();
	}

	public static SettingsProjectManager getInstance(Connection conn)
			throws SQLException {
		return new SettingsProjectManager(conn);
	}

}

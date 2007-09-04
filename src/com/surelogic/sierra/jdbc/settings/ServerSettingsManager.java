package com.surelogic.sierra.jdbc.settings;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.Settings;
import com.surelogic.sierra.tool.message.SettingsReply;

public class ServerSettingsManager extends SettingsManager {

	private final PreparedStatement getSettingsByName;
	private final PreparedStatement getSettingsByProject;
	private final PreparedStatement getLatestSettingsByProject;
	private final PreparedStatement updateSettings;

	private ServerSettingsManager(Connection conn) throws SQLException {
		super(conn);
		getSettingsByName = conn
				.prepareStatement("SELECT SETTINGS FROM SETTINGS WHERE NAME = ?");
		getLatestSettingsByProject = conn
				.prepareStatement("SELECT S.REVISION,S.SETTINGS FROM PROJECT P, PROJECT_SETTINGS_RELTN PSR, SETTINGS S WHERE P.NAME = ? AND PSR.PROJECT_ID = P.ID AND S.NAME = PSR.SETTINGS_NAME AND S.REVISION > ?");
		getSettingsByProject = conn
				.prepareStatement("SELECT S.SETTINGS FROM PROJECT P, PROJECT_SETTINGS_RELTN PSR, SETTINGS S WHERE P.NAME = ? AND PSR.PROJECT_ID = P.ID AND S.NAME = PSR.SETTINGS_NAME");
		updateSettings = conn
				.prepareStatement("UPDATE SETTINGS SET REVISION = ?, SETTINGS = ? WHERE NAME = ?");
	}

	public static ServerSettingsManager getInstance(Connection conn)
			throws SQLException {
		return new ServerSettingsManager(conn);
	}

	public Settings getSettingsByName(String name) throws SQLException {
		getSettingsByName.setString(1, name);
		ResultSet set = getSettingsByName.executeQuery();
		if (set.next()) {
			Clob clob = set.getClob(1);
			if (clob != null) {
				return mw.fetchSettings(clob.getCharacterStream());
			}
		}
		return null;
	}

	public Settings getSettingsByProject(String project) throws SQLException {
		getSettingsByProject.setString(1, project);
		ResultSet set = getSettingsByProject.executeQuery();
		if (set.next()) {
			Clob clob = set.getClob(1);
			if (clob != null) {
				return mw.fetchSettings(clob.getCharacterStream());
			}
		}
		return null;
	}

	/**
	 * Return the latest settings for a given project, but ONLY if there is a
	 * more recent set of settings than the given ones.
	 * 
	 * @param project
	 * @param revision
	 * @return
	 * @throws SQLException
	 */
	public SettingsReply getLatestSettingsByProject(String project,
			Long revision) throws SQLException {
		SettingsReply reply = new SettingsReply();
		getLatestSettingsByProject.setString(1, project);
		getLatestSettingsByProject.setLong(2, revision);
		ResultSet set = getLatestSettingsByProject.executeQuery();
		if (set.next()) {
			reply.setRevision(set.getLong(1));
			Clob clob = set.getClob(2);
			if (clob != null) {
				reply.setSettings(mw.fetchSettings(clob.getCharacterStream()));
			}

		}
		return reply;
	}

	public void writeSettings(String name, Long revision, Settings settings)
			throws SQLException {
		StringWriter writer = new StringWriter();
		mw.writeSettings(settings, writer);
		String str = writer.toString();
		StringReader reader = new StringReader(str);
		updateSettings.setLong(1, revision);
		updateSettings.setCharacterStream(2, reader, str.length());
		updateSettings.executeQuery();
	}
}

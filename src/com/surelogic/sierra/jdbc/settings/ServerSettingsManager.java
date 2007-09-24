package com.surelogic.sierra.jdbc.settings;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.surelogic.sierra.tool.message.Settings;
import com.surelogic.sierra.tool.message.SettingsReply;

public class ServerSettingsManager extends SettingsManager {

	private static final String FIND_ALL = "SELECT NAME FROM SETTINGS";

	private final PreparedStatement getSettingsByName;
	private final PreparedStatement getSettingsByProject;
	private final PreparedStatement getLatestSettingsByProject;
	private final PreparedStatement updateSettings;
	private final PreparedStatement getAllSettings;

	private final PreparedStatement createNewSetting;

	private ServerSettingsManager(Connection conn) throws SQLException {
		super(conn);
		getSettingsByName = conn
				.prepareStatement("SELECT SETTINGS FROM SETTINGS WHERE NAME = ?");
		getLatestSettingsByProject = conn
				.prepareStatement("SELECT S.REVISION,S.SETTINGS FROM PROJECT P, PROJECT_SETTINGS_RELTN PSR, SETTINGS S WHERE P.NAME = ? AND PSR.PROJECT_ID = P.ID AND S.ID = PSR.SETTINGS_ID AND S.REVISION > ?");
		getSettingsByProject = conn
				.prepareStatement("SELECT S.SETTINGS FROM PROJECT P, PROJECT_SETTINGS_RELTN PSR, SETTINGS S WHERE P.NAME = ? AND PSR.PROJECT_ID = P.ID AND S.ID = PSR.SETTINGS_ID");
		updateSettings = conn
				.prepareStatement("UPDATE SETTINGS SET REVISION = ?, SETTINGS = ? WHERE NAME = ?");
		getAllSettings = conn.prepareStatement(FIND_ALL);

		createNewSetting = conn
				.prepareStatement("INSERT INTO SETTINGS (NAME, REVISION, SETTINGS) VALUES (?,?,?)");
	}

	public static ServerSettingsManager getInstance(Connection conn)
			throws SQLException {
		return new ServerSettingsManager(conn);
	}

	public int add(String name) throws SQLException {
		// XXX insert a new row into the revision table w/ the current vm time
		// and use the generated key as your revision number
		Long revision = new Long(0);
		Settings settings = new Settings();

		StringWriter writer = new StringWriter();
		mw.writeSettings(settings, writer);
		String str = writer.toString();
		StringReader reader = new StringReader(str);

		createNewSetting.setString(1, name);
		createNewSetting.setLong(2, revision);
		createNewSetting.setCharacterStream(2, reader, str.length());
		return 0;
	}

	/**
	 * 
	 * @return a collection of all the product names
	 * @throws SQLException
	 */
	public Collection<String> getAllSettingNames() throws SQLException {
		ResultSet rs = getAllSettings.executeQuery();
		Collection<String> settingNames = new ArrayList<String>();
		while (rs.next()) {
			settingNames.add(rs.getString(1));
		}
		return settingNames;
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

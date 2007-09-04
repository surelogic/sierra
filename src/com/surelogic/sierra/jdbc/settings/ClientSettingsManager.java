package com.surelogic.sierra.jdbc.settings;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.Settings;

public class ClientSettingsManager extends SettingsManager {

	private final PreparedStatement getSettings;
	private final PreparedStatement updateSettings;

	private ClientSettingsManager(Connection conn) throws SQLException {
		super(conn);
		getSettings = conn
				.prepareStatement("SELECT SETTINGS FROM PROJECT WHERE NAME = ?");
		updateSettings = conn
				.prepareStatement("UPDATE PROJECT SET SETTINGS_REVISION = ?, SETTINGS = ? WHERE NAME = ?");
	}

	public Settings getSettings(String project) throws SQLException {
		getSettings.setString(1, project);
		ResultSet set = getSettings.executeQuery();
		if (set.next()) {
			Clob clob = set.getClob(1);
			if (clob != null) {
				return mw.fetchSettings(clob.getCharacterStream());
			}
		}
		return null;
	}

	public void writeSettings(String project, Long revision, Settings settings)
			throws SQLException {
		StringWriter writer = new StringWriter();
		mw.writeSettings(settings, writer);
		String str = writer.toString();
		StringReader reader = new StringReader(str);
		updateSettings.setLong(1, revision);
		updateSettings.setCharacterStream(2, reader, str.length());
		updateSettings.executeQuery();
	}

	public static ClientSettingsManager getInstance(Connection conn)
			throws SQLException {
		return new ClientSettingsManager(conn);
	}
}

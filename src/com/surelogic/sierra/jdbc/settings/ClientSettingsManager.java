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

		private final PreparedStatement getSettingsRevision;

	private ClientSettingsManager(Connection conn) throws SQLException {
		super(conn);
		getSettingsRevision = conn
				.prepareStatement("SELECT SETTINGS_REVISION FROM PROJECT WHERE NAME = ?");
	}

	public Settings getSettings(String project) throws SQLException {
//		getSettings.setString(1, project);
//		ResultSet set = getSettings.executeQuery();
//		if (set.next()) {
//			Clob clob = set.getClob(1);
//			if (clob != null) {
//				return mw.fetchSettings(clob.getCharacterStream());
//			}
//		}
		return new Settings();
	}

	public Long getSettingsRevision(String project) throws SQLException {
		getSettingsRevision.setString(1, project);
		ResultSet set = getSettingsRevision.executeQuery();
		if (set.next()) {
			return set.getLong(1);
		} else {
			throw new IllegalArgumentException("No project with name "
					+ project + " exists");
		}
	}

	public void writeSettings(String project, Long revision, Settings settings)
			throws SQLException {

	}

	public static ClientSettingsManager getInstance(Connection conn)
			throws SQLException {
		return new ClientSettingsManager(conn);
	}
}

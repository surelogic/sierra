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
import java.util.List;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Settings;
import com.surelogic.sierra.tool.message.SettingsReply;

public class ServerSettingsManager extends SettingsManager {

	private static final String FIND_ALL = "SELECT NAME FROM SETTINGS";

	private final PreparedStatement deleteFindingTypeFilter;
	private final PreparedStatement insertFindingTypeFilter;
	private final PreparedStatement getFiltersBySettingId;
	private final PreparedStatement getFiltersBySettingIdAndCategory;
	private final PreparedStatement getSettingsByProject;
	private final PreparedStatement getLatestSettingsByProject;
	private final PreparedStatement updateSettings;
	private final PreparedStatement getAllSettings;

	private final BaseMapper settingsMapper;

	private ServerSettingsManager(Connection conn) throws SQLException {
		super(conn);
		deleteFindingTypeFilter = conn
				.prepareStatement("DELETE FROM SETTING_FILTERS WHERE SETTINGS_ID = ? AND FINDING_TYPE_ID = ?");
		insertFindingTypeFilter = conn
				.prepareStatement("INSERT INTO SETTING_FILTERS (SETTINGS_ID, FINDING_TYPE_ID,DELTA,IMPORTANCE,FILTERED) VALUES (?,?,?,?,?)");
		getFiltersBySettingId = conn
				.prepareStatement("SELECT FT.UID,F.DELTA,F.IMPORTANCE,F.FILTERED FROM SETTING_FILTERS F, FINDING_TYPE FT WHERE F.SETTINGS_ID = ? FT.ID = F.FINDING_TYPE_ID");
		getFiltersBySettingIdAndCategory = conn
				.prepareStatement("SELECT FT.UID,F.DELTA,F.IMPORTANCE,F.FILTERED FROM CATEGORY C, CATEGORY_FINDING_TYPE_RELTN CFR, SETTING_FILTERS F, FINDING_TYPE FT WHERE C.NAME = ? AND CFR.CATEGORY_ID = C.ID AND AND F.SETTINGS_ID = ? AND F.FINDING_TYPE_ID = CFR.FINDING_TYPE_ID AND FT.ID = F.FINDING_TYPE_ID");
		getLatestSettingsByProject = conn
				.prepareStatement("SELECT S.REVISION,S.SETTINGS FROM PROJECT P, PROJECT_SETTINGS_RELTN PSR, SETTINGS S WHERE P.NAME = ? AND PSR.PROJECT_ID = P.ID AND S.ID = PSR.SETTINGS_ID AND S.REVISION > ?");
		getSettingsByProject = conn
				.prepareStatement("SELECT S.SETTINGS FROM PROJECT P, PROJECT_SETTINGS_RELTN PSR, SETTINGS S WHERE P.NAME = ? AND PSR.PROJECT_ID = P.ID AND S.ID = PSR.SETTINGS_ID");
		updateSettings = conn
				.prepareStatement("UPDATE SETTINGS SET REVISION = ?, SETTINGS = ? WHERE NAME = ?");
		getAllSettings = conn.prepareStatement(FIND_ALL);
		settingsMapper = new BaseMapper(conn,
				"INSERT INTO SETTINGS (NAME, REVISION) VALUES (?,?)",
				"SELECT ID,REVISION FROM SETTINGS WHERE NAME = ?",
				"DELETE FROM SETTINGS WHERE ID = ?");
	}

	public static ServerSettingsManager getInstance(Connection conn)
			throws SQLException {
		return new ServerSettingsManager(conn);
	}

	/**
	 * Create a new group of settings.
	 * 
	 * @param name
	 */
	public void createSettings(String name) {

	}

	/**
	 * Get the list of categories that settings can be defined for.
	 * 
	 * @param settings
	 * @return
	 */
	public List<String> listSettingCategories(String settings) {
		return null;
	}

	/**
	 * Return the current finding type filters for the selected category. All
	 * finding types in this category are guaranteed to have a filter.
	 * 
	 * @param category
	 * @param settings
	 * @return
	 */
	public List<FindingTypeFilter> listCategoryFilters(String category,
			String settings) {
		return null;
	}

	/**
	 * Insert the specified filters into these settings. Any existing finding
	 * type filters for the selected finding types will be deleted.
	 * 
	 * @param filters
	 * @param settings
	 */
	public void applyFilters(List<FindingTypeFilter> filters, String settings) {

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

	public void writeSettings(String name, Settings settings)
			throws SQLException {
		StringWriter writer = new StringWriter();
		mw.writeSettings(settings, writer);
		String str = writer.toString();
		StringReader reader = new StringReader(str);
		updateSettings.setLong(1, Server.nextRevision(conn));
		updateSettings.setCharacterStream(2, reader, str.length());
		updateSettings.executeQuery();
	}

	private FindingTypeFilter readFilter(ResultSet set) throws SQLException {
		FindingTypeFilter filter = new FindingTypeFilter();
		filter.setName(set.getString(1));
		boolean hasImportance;
		boolean hasFiltered;
		int delta = set.getInt(2);
		int importance = set.getInt(3);
		hasImportance = !set.wasNull();
		if (hasImportance) {
			filter.setImportance(Importance.values()[importance]);
		}
		String filtered = set.getString(4);
		hasFiltered = !set.wasNull();
		if (hasFiltered) {
			filter.setFiltered("Y".equals(filtered));
		}
		if (!(hasImportance || hasFiltered)) {
			filter.setDelta(delta);
		}
		return filter;
	}
}

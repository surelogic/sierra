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

import org.apache.tools.ant.util.facade.FacadeTaskHelper;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.CategoryRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeFilterRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeRecord;
import com.surelogic.sierra.jdbc.record.SettingsRecord;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.tool.FindingTypeRecordFactory;
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
	private final PreparedStatement listSettingCategories;
	private final PreparedStatement getSettingsByProject;
	private final PreparedStatement getLatestSettingsByProject;
	private final PreparedStatement updateSettings;
	private final PreparedStatement getAllSettings;

	private final BaseMapper settingsMapper;

	private final FindingTypeRecordFactory ftFactory;

	private ServerSettingsManager(Connection conn) throws SQLException {
		super(conn);
		listSettingCategories = conn
				.prepareStatement("SELECT UID,NAME FROM FINDING_CATEGORY");
		deleteFindingTypeFilter = conn
				.prepareStatement("DELETE FROM SETTING_FILTERS WHERE SETTINGS_ID = ? AND FINDING_TYPE_ID = ?");
		insertFindingTypeFilter = conn
				.prepareStatement("INSERT INTO SETTING_FILTERS (SETTINGS_ID, FINDING_TYPE_ID,DELTA,IMPORTANCE,FILTERED) VALUES (?,?,?,?,?)");
		getFiltersBySettingId = conn
				.prepareStatement("SELECT FT.UID,F.DELTA,F.IMPORTANCE,F.FILTERED FROM SETTING_FILTERS F, FINDING_TYPE FT WHERE F.SETTINGS_ID = ? AND FT.ID = F.FINDING_TYPE_ID");
		getFiltersBySettingIdAndCategory = conn
				.prepareStatement("SELECT FT.UID,F.DELTA,F.IMPORTANCE,F.FILTERED FROM "
						+ "FINDING_CATEGORY C INNER JOIN CATEGORY_FINDING_TYPE_RELTN CFR ON CFR.CATEGORY_ID = C.ID"
						+ " INNER JOIN FINDING_TYPE FT ON FT.ID = F.FINDING_TYPE_ID "
						+ " LEFT OUTER JOIN SETTING_FILTERS F ON F.FINDING_TYPE_ID = CFR.FINDING_TYPE_ID"
						+ " WHERE C.ID = ? AND F.SETTINGS_ID = ?");
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
		ftFactory = FindingTypeRecordFactory.getInstance(conn);
	}

	public static ServerSettingsManager getInstance(Connection conn)
			throws SQLException {
		return new ServerSettingsManager(conn);
	}

	/**
	 * Create a new group of settings. If the settings already exist, this
	 * method does nothing.
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public void createSettings(String name) throws SQLException {
		SettingsRecord record = newSettingsRecord();
		record.setName(name);
		if (!record.select()) {
			record.setRevision(Server.nextRevision(conn));
			record.insert();
		} else {
			throw new IllegalArgumentException("Settings with the name " + name
					+ " already exist.");
		}
	}

	/**
	 * Get the list of categories that settings can be defined for.
	 * 
	 * @param settings
	 * @return
	 * @throws SQLException
	 */
	public List<CategoryView> listSettingCategories(String settings)
			throws SQLException {
		ResultSet set = listSettingCategories.executeQuery();
		List<CategoryView> view = new ArrayList<CategoryView>();
		while (set.next()) {
			view.add(new CategoryView(set.getString(1), set.getString(2)));
		}
		return view;
	}

	/**
	 * Return the current finding type filters for the selected category. All
	 * finding types in this category are guaranteed to have a filter.
	 * 
	 * @param category
	 *            a category uid
	 * @param settings
	 *            a settings name
	 * @return
	 * @throws SQLException
	 */
	public List<FindingTypeFilter> listCategoryFilters(String category,
			String settings) throws SQLException {
		CategoryRecord cRec = ftFactory.newCategoryRecord();
		cRec.setUid(category);
		if (cRec.select()) {
			SettingsRecord sRec = newSettingsRecord();
			sRec.setName(settings);
			if (sRec.select()) {
				List<FindingTypeFilter> filters = new ArrayList<FindingTypeFilter>();
				getFiltersBySettingIdAndCategory.setLong(1, sRec.getId());
				getFiltersBySettingIdAndCategory.setLong(2, cRec.getId());
				ResultSet set = getFiltersBySettingIdAndCategory.executeQuery();
				while (set.next()) {
					filters.add(readFilter(set));
				}
				return filters;
			} else {
				throw new IllegalArgumentException(settings
						+ " is not a valid settings name");
			}
		} else {
			throw new IllegalArgumentException("The category with uid "
					+ category + " is not a valid category.");
		}
	}

	/**
	 * Insert the specified filters into these settings. Any existing finding
	 * type filters for the selected finding types will be deleted.
	 * 
	 * @param filters
	 * @param settings
	 * @throws SQLException
	 */
	public void applyFilters(List<FindingTypeFilter> filters, String settings)
			throws SQLException {
		SettingsRecord sRec = newSettingsRecord();
		sRec.setName(settings);
		if (sRec.select()) {
			for (FindingTypeFilter filter : filters) {
				FindingTypeRecord ftRec = ftFactory.newFindingTypeRecord();
				ftRec.setUid(filter.getName());
				if (ftRec.select()) {
					FindingTypeFilterRecord rec = newFilterRecord();
					rec.setId(new FindingTypeFilterRecord.PK(sRec.getId(),
							ftRec.getId()));
					
				} else {
					throw new IllegalArgumentException(filter.getName()
							+ " is not a valid filter name.");
				}
			}
		} else {
			throw new IllegalArgumentException(settings
					+ " is not a valid settings name");
		}
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

	private FindingTypeFilterRecord newFilterRecord() {
		return new FindingTypeFilterRecord(null);
	}

	private SettingsRecord newSettingsRecord() {
		return new SettingsRecord(settingsMapper);
	}

	private FindingTypeFilter readFilter(ResultSet set) throws SQLException {
		int idx = 1;
		FindingTypeFilter filter = new FindingTypeFilter();
		filter.setName(set.getString(idx++));
		boolean hasImportance;
		boolean hasFiltered;
		int delta = set.getInt(idx++);
		int importance = set.getInt(idx++);
		hasImportance = !set.wasNull();
		if (hasImportance) {
			filter.setImportance(Importance.values()[importance]);
		}
		String filtered = set.getString(idx++);
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

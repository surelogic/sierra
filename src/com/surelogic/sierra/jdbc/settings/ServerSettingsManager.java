package com.surelogic.sierra.jdbc.settings;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.CategoryRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeFilterRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeRecord;
import com.surelogic.sierra.jdbc.record.SettingsRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.tool.FindingTypeRecordFactory;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Settings;
import com.surelogic.sierra.tool.message.SettingsReply;

public class ServerSettingsManager extends SettingsManager {

	private static final String FIND_ALL = "SELECT NAME FROM SETTINGS";

	private final PreparedStatement getFiltersBySettingId;
	private final PreparedStatement getFiltersBySettingIdAndCategory;
	private final PreparedStatement listSettingCategories;
	private final PreparedStatement getSettingsByProject;
	private final PreparedStatement copySettings;
	private final PreparedStatement getLatestSettingsByProject;
	private final PreparedStatement getAllSettings;

	private final UpdateBaseMapper settingsMapper;
	private final BaseMapper findingTypeFilterMapper;

	private final FindingTypeRecordFactory ftFactory;

	private ServerSettingsManager(Connection conn) throws SQLException {
		super(conn);
		listSettingCategories = conn
				.prepareStatement("SELECT UID,NAME FROM FINDING_CATEGORY");
		findingTypeFilterMapper = new BaseMapper(
				conn,
				"INSERT INTO SETTING_FILTERS (SETTINGS_ID, FINDING_TYPE_ID,DELTA,IMPORTANCE,FILTERED) VALUES (?,?,?,?,?)",
				"SELECT DELTA,IMPORTANCE,FILTERED FROM SETTING_FILTERS WHERE SETTINGS_ID = ? AND FINDING_TYPE_ID = ?",
				"DELETE FROM SETTING_FILTERS WHERE SETTINGS_ID = ? AND FINDING_TYPE_ID = ?");
		getFiltersBySettingId = conn
				.prepareStatement("SELECT FT.UID,F.DELTA,F.IMPORTANCE,F.FILTERED FROM SETTING_FILTERS F, FINDING_TYPE FT WHERE F.SETTINGS_ID = ? AND FT.ID = F.FINDING_TYPE_ID");
		getFiltersBySettingIdAndCategory = conn
				.prepareStatement("SELECT FT.UID,F.DELTA,F.IMPORTANCE,F.FILTERED FROM "
						+ "FINDING_CATEGORY C INNER JOIN CATEGORY_FINDING_TYPE_RELTN CFR ON CFR.CATEGORY_ID = C.ID"
						+ " INNER JOIN FINDING_TYPE FT ON FT.ID = CFR.FINDING_TYPE_ID "
						+ " LEFT OUTER JOIN SETTING_FILTERS F ON F.FINDING_TYPE_ID = FT.ID AND F.SETTINGS_ID = ?"
						+ " WHERE C.ID = ?");
		getLatestSettingsByProject = conn
				.prepareStatement("SELECT S.ID, S.REVISION FROM SETTINGS_PROJECT_RELTN PSR, SETTINGS S WHERE PSR.PROJECT_NAME = ? AND S.ID = PSR.SETTINGS_ID AND S.REVISION > ?");
		getSettingsByProject = conn
				.prepareStatement("SELECT S.ID FROM SETTINGS_PROJECT_RELTN PSR, SETTINGS S WHERE PSR.PROJECT_NAME = ? AND S.ID = PSR.SETTINGS_ID");
		copySettings = conn
				.prepareStatement("INSERT INTO SETTING_FILTERS SELECT ?,FINDING_TYPE_ID,FILTER_TYPE,DELTA,IMPORTANCE,FILTERED FROM SETTING_FILTERS WHERE SETTINGS_ID = ?");
		getAllSettings = conn.prepareStatement(FIND_ALL);
		settingsMapper = new UpdateBaseMapper(conn,
				"INSERT INTO SETTINGS (NAME, REVISION) VALUES (?,?)",
				"SELECT ID,REVISION FROM SETTINGS WHERE NAME = ?",
				"DELETE FROM SETTINGS WHERE ID = ?",
				"UPDATE SETTINGS SET NAME = ? WHERE ID = ?");
		ftFactory = FindingTypeRecordFactory.getInstance(conn);
	}

	public static ServerSettingsManager getInstance(Connection conn)
			throws SQLException {
		return new ServerSettingsManager(conn);
	}

	/**
	 * Create a new settings record, and optionally pre-populates it from
	 * existing settings.
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public void createSettings(String name) throws SQLException {
		createSettings(name, null);
	}

	/**
	 * Create a new settings record, and optionally pre-populates it from
	 * existing settings.
	 * 
	 * @param name
	 * @param from
	 *            the name of an existing settings record that we want to copy
	 *            from. May be null.
	 * @throws SQLException
	 */
	public void createSettings(String name, String from) throws SQLException {
		SettingsRecord record = newSettingsRecord();
		record.setName(name);
		if (!record.select()) {
			record.setRevision(Server.nextRevision(conn));
			record.insert();
			if (from != null) {
				SettingsRecord old = newSettingsRecord();
				old.setName(from);
				if (old.select()) {
					copySettings.setLong(1, record.getId());
					copySettings.setLong(2, record.getId());
					copySettings.execute();
				} else {
					throw new IllegalArgumentException(
							"Settings with the name " + from
									+ " already exist.");
				}
			}
		} else {
			throw new IllegalArgumentException("Settings with the name " + name
					+ " already exist.");
		}
	}

	public void renameSettings(String currName, String newName)
			throws SQLException {

		SettingsRecord record = newSettingsRecord();
		record.setName(currName);

		/** Can't rename a setting which does not exist */
		if (!record.select()) {
			// XXX fill in
			throw new SQLException();
		}

		record.setName(newName);
		record.update();
	}

	public void deleteSettings(String settings) throws SQLException {
		SettingsRecord record = newSettingsRecord();
		record.setName(settings);

		/** If this product does not exist, throw an error */
		if (!record.select()) {
			// XXX Throw error
			throw new SQLException();
		}

		record.delete();
	}

	/**
	 * Get the list of categories that settings can be defined for.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<CategoryView> listSettingCategories() throws SQLException {
		ResultSet set = listSettingCategories.executeQuery();
		List<CategoryView> view = new ArrayList<CategoryView>();
		while (set.next()) {
			view.add(new CategoryView(set.getString(1), set.getString(2)));
		}
		set.close();
		return view;
	}

	/**
	 * Return the current finding type filters for the selected category. All
	 * finding types in this category are guaranteed to have a filter. If none
	 * exists, an filter is created with a delta of 0.
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
				set.close();
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
					if (rec.select()) {
						rec.delete();
					}
					if ((filter.getImportance() != null)
							|| (filter.isFiltered() != null)
							|| ((filter.getDelta() != null) && (filter
									.getDelta() != 0))) {
						rec.setImportance(filter.getImportance());
						rec.setFiltered(filter.isFiltered());
						rec.setDelta(filter.getDelta());
						rec.insert();
					}
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
		rs.close();
		return settingNames;
	}

	public Settings getSettingsByName(String name) throws SQLException {
		SettingsRecord rec = newSettingsRecord();
		rec.setName(name);
		if (rec.select()) {
			getFiltersBySettingId.setLong(1, rec.getId());
			return readSettings(getFiltersBySettingId.executeQuery());
		} else {
			return null;
		}
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
			Long settings = set.getLong(1);
			reply.setRevision(set.getLong(2));
			getFiltersBySettingId.setLong(1, settings);
			reply
					.setSettings(readSettings(getFiltersBySettingId
							.executeQuery()));
		}
		set.close();
		return reply;
	}

	public void writeSettings(Settings settings, String name)
			throws SQLException {
		applyFilters(settings.getFilter(), name);
	}

	/**
	 * Return the current settings for the given project.
	 * @param projectName
	 * @return
	 * @throws SQLException
	 */
	public Settings getSettingsByProject(String projectName)
			throws SQLException {
		getSettingsByProject.setString(1, projectName);
		ResultSet set = getSettingsByProject.executeQuery();
		Settings settings;
		if (set.next()) {
			getFiltersBySettingId.setLong(1, set.getLong(1));
			settings = readSettings(getFiltersBySettingId.executeQuery());
		} else {
			settings = new Settings();
		}
		set.close();
		return settings;
	}

	private FindingTypeFilterRecord newFilterRecord() {
		return new FindingTypeFilterRecord(findingTypeFilterMapper);
	}

	private SettingsRecord newSettingsRecord() {
		return new SettingsRecord(settingsMapper);
	}

	private Settings readSettings(ResultSet set) throws SQLException {
		Settings settings = new Settings();
		List<FindingTypeFilter> filters = settings.getFilter();
		while (set.next()) {
			filters.add(readFilter(set));
		}
		set.close();
		return settings;
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

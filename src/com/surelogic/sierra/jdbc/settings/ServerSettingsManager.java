package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.FindingTypeFilterRecord;
import com.surelogic.sierra.jdbc.record.SettingsRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Settings;
import com.surelogic.sierra.tool.message.SettingsReply;

public class ServerSettingsManager extends SettingsManager {

	private static final String FIND_ALL = "SELECT NAME FROM SETTINGS";

	private final PreparedStatement deleteFilterByFindingType;
	private final PreparedStatement getFiltersBySettingId;
	private final PreparedStatement getFiltersBySettingIdAndCategory;
	private final PreparedStatement listSettingCategories;
	private final PreparedStatement getSettingsByProject;
	private final PreparedStatement copySettings;
	private final PreparedStatement getLatestSettingsByProject;
	private final PreparedStatement getAllSettings;

	private final UpdateBaseMapper settingsMapper;
	private final BaseMapper findingTypeFilterMapper;

	private final SettingsProjectManager spManager;

	private ServerSettingsManager(Connection conn) throws SQLException {
		super(conn);
		listSettingCategories = conn
				.prepareStatement("SELECT UUID,NAME FROM FINDING_CATEGORY");
		findingTypeFilterMapper = new BaseMapper(
				conn,
				"INSERT INTO SETTING_FILTERS (SETTINGS_ID, FINDING_TYPE_ID,DELTA,IMPORTANCE,FILTERED) VALUES (?,?,?,?,?)",
				null, null, false);
		this.deleteFilterByFindingType = conn
				.prepareStatement("DELETE FROM SETTING_FILTERS WHERE SETTINGS_ID = ? AND FINDING_TYPE_ID = ?");
		getFiltersBySettingId = conn
				.prepareStatement("SELECT FT.UUID,F.DELTA,F.IMPORTANCE,F.FILTERED FROM SETTING_FILTERS F, FINDING_TYPE FT WHERE F.SETTINGS_ID = ? AND FT.ID = F.FINDING_TYPE_ID");
		getFiltersBySettingIdAndCategory = conn
				.prepareStatement("SELECT FT.UUID,F.DELTA,F.IMPORTANCE,F.FILTERED FROM"
						+ " FINDING_TYPE FT LEFT OUTER JOIN SETTING_FILTERS F"
						+ " ON F.FINDING_TYPE_ID = FT.ID AND F.SETTINGS_ID = ?"
						+ " WHERE FT.CATEGORY_ID = ?");
		getLatestSettingsByProject = conn
				.prepareStatement("SELECT S.ID, S.REVISION FROM SETTINGS_PROJECT_RELTN PSR, SETTINGS S WHERE PSR.PROJECT_NAME = ? AND S.ID = PSR.SETTINGS_ID AND S.REVISION > ?");
		getSettingsByProject = conn
				.prepareStatement("SELECT S.ID FROM SETTINGS_PROJECT_RELTN PSR, SETTINGS S WHERE PSR.PROJECT_NAME = ? AND S.ID = PSR.SETTINGS_ID");
		copySettings = conn
				.prepareStatement("INSERT INTO SETTING_FILTERS SELECT ?,FINDING_TYPE_ID,DELTA,IMPORTANCE,FILTERED FROM SETTING_FILTERS WHERE SETTINGS_ID = ?");
		getAllSettings = conn.prepareStatement(FIND_ALL);
		settingsMapper = new UpdateBaseMapper(conn,
				"INSERT INTO SETTINGS (NAME, REVISION) VALUES (?,?)",
				"SELECT ID,REVISION FROM SETTINGS WHERE NAME = ?",
				"DELETE FROM SETTINGS WHERE ID = ?",
				"UPDATE SETTINGS SET NAME = ?, REVISION = ? WHERE ID = ?");
		spManager = SettingsProjectManager.getInstance(conn);
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
		try {
			while (set.next()) {
				view.add(new CategoryView(set.getString(1), set.getString(2)));
			}
		} finally {
			set.close();
		}
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
	public List<FindingTypeFilterDetail> listCategoryFilters(String category,
			String settings) throws SQLException {
		Long categoryId = ftMan.getCategoryId(category);
		if (categoryId != null) {
			SettingsRecord sRec = newSettingsRecord();
			sRec.setName(settings);
			if (sRec.select()) {
				List<FindingTypeFilterDetail> filters = new ArrayList<FindingTypeFilterDetail>();
				getFiltersBySettingIdAndCategory.setLong(1, sRec.getId());
				getFiltersBySettingIdAndCategory.setLong(2, categoryId);
				ResultSet set = getFiltersBySettingIdAndCategory.executeQuery();
				try {
					while (set.next()) {
						FindingTypeFilter f = readFilter(set);
						FindingType ft = ftMan.getFindingType(f.getName());
						FindingTypeFilterDetail fd = new FindingTypeFilterDetail();
						fd.setDelta(f.getDelta());
						fd.setDescription(ft.getInfo());
						fd.setFiltered(f.isFiltered());
						fd.setImportance(f.getImportance());
						fd.setName(ft.getName());
						fd.setUid(f.getName());
						filters.add(fd);
					}
				} finally {
					set.close();
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
			applyFilters(sRec.getId(), filters);
			sRec.setRevision(Server.nextRevision(conn));
			sRec.update();
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
		try {
			while (rs.next()) {
				settingNames.add(rs.getString(1));
			}
		} finally {
			rs.close();
		}
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
		try {
			if (set.next()) {
				Long settings = set.getLong(1);
				reply.setRevision(set.getLong(2));
				getFiltersBySettingId.setLong(1, settings);
				reply.setSettings(readSettings(getFiltersBySettingId
						.executeQuery()));
			}
		} finally {
			set.close();
		}
		return reply;
	}

	public void writeSettings(Settings settings, String name)
			throws SQLException {
		applyFilters(settings.getFilter(), name);
	}

	/**
	 * Return the current settings for the given project.
	 * 
	 * @param projectName
	 * @return
	 * @throws SQLException
	 */
	public Settings getSettingsByProject(String projectName)
			throws SQLException {
		getSettingsByProject.setString(1, projectName);
		ResultSet set = getSettingsByProject.executeQuery();
		try {
			if (set.next()) {
				getFiltersBySettingId.setLong(1, set.getLong(1));
				return readSettings(getFiltersBySettingId.executeQuery());
			} else {
				return new Settings();
			}
		} finally {
			set.close();
		}
	}

	public void addProjects(String settingsName, Collection<String> projects)
			throws SQLException {

		if (settingsName == null)
			throw new SQLException();

		SettingsRecord rec = newSettingsRecord();
		rec.setName(settingsName);

		/** If this settings file does not exist, throw an error */
		if (!rec.select()) {
			// XXX Throw error
			throw new SQLException();
		}

		addProjects(rec, projects);
	}

	public void deleteProjectRelation(String settingsName, String projectName)
			throws SQLException {
		if (settingsName == null)
			throw new SQLException();

		SettingsRecord rec = newSettingsRecord();
		rec.setName(settingsName);

		/** If this settings file does not exist, throw an error */
		if (!rec.select()) {
			// XXX Throw error
			throw new SQLException();
		}

		spManager.deleteProjectRelation(rec, projectName);
	}

	@Override
	protected PreparedStatement getDeleteFilterByFindingType() {
		return deleteFilterByFindingType;
	}

	@Override
	protected FindingTypeFilterRecord newFilterRecord() {
		return new FindingTypeFilterRecord(findingTypeFilterMapper);
	}

	private SettingsRecord newSettingsRecord() {
		return new SettingsRecord(settingsMapper);
	}

	private void addProjects(SettingsRecord settings,
			Collection<String> projects) throws SQLException {
		if (projects != null) {
			for (String projectName : projects) {
				spManager.addRelation(settings, projectName);
			}
		}
	}

}

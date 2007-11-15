package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.FilterSetRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeFilterRecord;
import com.surelogic.sierra.jdbc.record.SettingsRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.tool.message.FilterEntry;
import com.surelogic.sierra.tool.message.FilterSet;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Settings;
import com.surelogic.sierra.tool.message.SettingsReply;

public class ServerSettingsManager extends SettingsManager {

	private static final String FIND_ALL = "SELECT NAME FROM SETTINGS";

	private final PreparedStatement deleteFilterByFindingType;
	private final PreparedStatement getFiltersBySettingId;
	private final PreparedStatement getFiltersBySettingIdAndCategory;
	private final PreparedStatement getSettingsByProject;
	private final PreparedStatement copySettings;
	private final PreparedStatement getLatestSettingsByProject;
	private final PreparedStatement getAllSettings;
	private final PreparedStatement listFilterSetIds;
	private final PreparedStatement loadFilterEntries;
	private final PreparedStatement loadFilterSetParents;
	private final PreparedStatement insertFilterSetParent;
	private final PreparedStatement insertFilterSetEntry;
	private final PreparedStatement getSettingsFilterSets;

	private final UpdateBaseMapper settingsMapper;
	private final BaseMapper findingTypeFilterMapper;
	private final UpdateRecordMapper filterSetMapper;

	private final SettingsProjectManager spManager;

	private ServerSettingsManager(Connection conn) throws SQLException {
		super(conn);
		filterSetMapper = new UpdateBaseMapper(
				conn,
				"INSERT INTO FILTER_SET (UUID,REVISION,NAME,INFO) VALUES (?,?,?,?)",
				"SELECT ID,REVISION,INFO FROM FILTER_SET WHERE NAME = ?",
				"DELETE FROM FILTER_SET WHERE ID = ?",
				"UPDATE FILTER_SET SET  REVISION = ?, NAME = ?, INFO = ? WHERE ID = ?");
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
				.prepareStatement("INSERT INTO SETTING_FILTER_SETS SELECT ?,FILTER_SET_ID FROM SETTING_FILTER_SETS WHERE SETTINGS_ID = ?");
		getAllSettings = conn.prepareStatement(FIND_ALL);
		settingsMapper = new UpdateBaseMapper(conn,
				"INSERT INTO SETTINGS (NAME, REVISION) VALUES (?,?)",
				"SELECT ID,REVISION FROM SETTINGS WHERE NAME = ?",
				"DELETE FROM SETTINGS WHERE ID = ?",
				"UPDATE SETTINGS SET NAME = ?, REVISION = ? WHERE ID = ?");
		spManager = SettingsProjectManager.getInstance(conn);
		loadFilterEntries = conn
				.prepareStatement("SELECT FT.UUID,FE.FILTERED FROM FILTER_ENTRY FE, FINDING_TYPE FT"
						+ "   WHERE FE.FILTER_SET_ID = ? AND FT.ID = FE.FINDING_TYPE_ID");
		loadFilterSetParents = conn
				.prepareStatement("SELECT PARENT_ID FROM FILTER_SET_RELTN WHERE CHILD_ID = ?");
		listFilterSetIds = conn.prepareStatement("SELECT UUID FROM FILTER_SET");
		insertFilterSetParent = conn
				.prepareStatement("INSERT INTO FILTER_SET_RELTN (CHILD_ID,PARENT_ID) VALUES (?,?)");
		insertFilterSetEntry = conn
				.prepareStatement("INSERT INTO FILTER_ENTRY (FILTER_SET_ID,FINDING_TYPE_ID,FILTERED) VALUES (?,?,?)");
		getSettingsFilterSets = conn
				.prepareStatement("SELECT UUID FROM SETTING_FILTER_SETS SFS, FILTER_SET FS WHERE SFS.SETTINGS_ID = ? AND FS.ID = SFS.FILTER_SET_ID");
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

	/**
	 * Change the name of the given settings
	 * 
	 * @param currName
	 * @param newName
	 * @throws SQLException
	 */
	public void renameSettings(String currName, String newName)
			throws SQLException {

		SettingsRecord record = newSettingsRecord();
		record.setName(currName);
		record.setRevision(Server.nextRevision(conn));

		/** Can't rename a setting which does not exist */
		if (!record.select()) {
			// XXX fill in
			throw new SQLException();
		}

		record.setName(newName);
		record.update();
	}

	/**
	 * Delete settings entirely.
	 * 
	 * @param settings
	 * @throws SQLException
	 */
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
	 * List the filter sets for these settings.
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public List<FilterSet> listSettingFilterSets(String name)
			throws SQLException {
		SettingsRecord settings = newSettingsRecord();
		settings.setName(name);
		if (settings.select()) {
			List<FilterSet> filterSets = new ArrayList<FilterSet>();
			getSettingsFilterSets.setLong(1, settings.getId());
			final ResultSet set = getSettingsFilterSets.executeQuery();
			try {
				while (set.next()) {
					filterSets.add(getFilterSet(set.getString(1)));
				}
			} finally {
				set.close();
			}
			return filterSets;
		} else {
			return null;
		}
	}

	public void writeFilterSet(FilterSet filterSet, long revision)
			throws SQLException {
		final FilterSetRecord filterSetRec = newFilterSetRecord();
		filterSetRec.setUid(filterSet.getUid());
		filterSetRec.setRevision(revision);
		filterSetRec.setName(filterSet.getName());
		filterSetRec.insert();
		final long filterSetId = filterSetRec.getId();
		final FilterSetRecord parentRec = newFilterSetRecord();
		for (final String parent : filterSet.getParent()) {
			parentRec.setUid(parent);
			if (parentRec.select()) {
				final long parentId = parentRec.getId();
				insertFilterSetParent.setLong(1, filterSetId);
				insertFilterSetParent.setLong(2, parentId);
				insertFilterSetParent.execute();
			} else {
				throw new IllegalArgumentException("Filter set with name "
						+ filterSet.getName() + " and uid "
						+ filterSet.getUid()
						+ "was being written, but parent with uid " + parent
						+ " could not be found");
			}
		}
		for (final FilterEntry entry : filterSet.getFilter()) {
			final long findingTypeId = ftMan.getFindingTypeId(entry.getType());
			insertFilterSetEntry.setLong(1, filterSetId);
			insertFilterSetEntry.setLong(2, findingTypeId);
			insertFilterSetEntry.setString(3, entry.isFiltered() ? "Y" : "N");
			insertFilterSetEntry.execute();
		}
	}

	/**
	 * Get the list of categories that settings can be defined for. This
	 * actually belong in the FindingTypeManager
	 * 
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
	public List<CategoryView> listSettingCategories() throws SQLException {
		return ftMan.listCategories();
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

	/**
	 * Retrieve settings by name.
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 */
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
	 * Return the latest settings for a given project, but ONLY if the settings
	 * have been updated since the given revision.
	 * 
	 * @param project
	 * @param revision
	 * @return
	 * @throws SQLException
	 */
	public SettingsReply getLatestSettingsByProject(String project,
			long revision) throws SQLException {
		SettingsReply reply = new SettingsReply();
		getLatestSettingsByProject.setString(1, project);
		getLatestSettingsByProject.setLong(2, revision);
		ResultSet set = getLatestSettingsByProject.executeQuery();
		try {
			if (set.next()) {
				long settings = set.getLong(1);
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

	/**
	 * List all of the filter sets available.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<FilterSet> listFilterSets() throws SQLException {
		final List<FilterSet> filterSets = new ArrayList<FilterSet>();
		final ResultSet set = listFilterSetIds.executeQuery();
		try {
			while (set.next()) {
				String uid = set.getString(1);
				filterSets.add(getFilterSet(uid));
			}
		} finally {
			set.close();
		}
		return filterSets;
	}

	/**
	 * Returns a FilterSet with the filter entries and parents populated, but
	 * not the name, uid, revision, or info.
	 * 
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	private FilterSet getFilterSetHelper(final long id) throws SQLException {
		final FilterSet filterSet = new FilterSet();
		final List<FilterEntry> filters = filterSet.getFilter();
		final List<String> parents = filterSet.getParent();
		loadFilterEntries.setLong(1, id);
		ResultSet set = loadFilterEntries.executeQuery();
		try {
			while (set.next()) {
				final FilterEntry entry = new FilterEntry();
				entry.setType(set.getString(1));
				entry.setFiltered("Y".equals(set.getString(2)));
				filters.add(entry);
			}
		} finally {
			set.close();
		}
		loadFilterSetParents.setLong(1, id);
		set = loadFilterSetParents.executeQuery();
		while (set.next()) {
			final String parent = set.getString(1);
			parents.add(parent);
		}
		return filterSet;
	}

	public FilterSet getFilterSet(final String uid) throws SQLException {
		FilterSetRecord rec = newFilterSetRecord();
		rec.setUid(uid);
		if (rec.select()) {
			FilterSet filterSet = getFilterSetHelper(rec.getId());
			filterSet.setName(rec.getName());
			filterSet.setUid(rec.getUid());
			return filterSet;
		} else {
			return null;
		}
	}

	/**
	 * Generates the setting finding type filters based off of the filter sets
	 * associated w/ these settings.
	 * 
	 * @param settingsId
	 * @throws SQLException
	 */
	private void regenerateFindingTypeFilters(final SettingsRecord settings)
			throws SQLException {
		final Map<String, FilterSet> filterSetMap = new HashMap<String, FilterSet>();
		final List<FilterSet> filterSets = new ArrayList<FilterSet>();
		/*
		 * Retrieve the filters sets directly associated with these settings as
		 * well as their ancestors.
		 */
		getSettingsFilterSets.setLong(1, settings.getId());
		final ResultSet set = getSettingsFilterSets.executeQuery();
		try {
			while (set.next()) {
				final String uid = set.getString(1);
				FilterSet filterSet = filterSetMap.get(uid);
				if (filterSet == null) {
					filterSet = getFilterSet(uid);
					filterSetMap.put(uid, filterSet);
				}
				filterSets.add(filterSet);
				for (final String parent : filterSet.getParent()) {
					if (filterSetMap.get(parent) == null) {
						filterSetMap.put(parent, getFilterSet(parent));
					}
				}
			}
		} finally {
			set.close();
		}
		final Set<String> findingTypes = new HashSet<String>();
		for (FilterSet filterSet : filterSets) {
			processFilterSet(findingTypes, filterSet, filterSetMap);
		}
		final List<FindingTypeFilter> findingTypeFilters = new ArrayList<FindingTypeFilter>();
		for (String uid : findingTypes) {
			final FindingTypeFilter ftf = new FindingTypeFilter();
			ftf.setFiltered(false);
			ftf.setName(uid);
			findingTypeFilters.add(ftf);
		}
		applyFilters(settings.getId(), findingTypeFilters);
	}

	/**
	 * This function recursively process filter sets, modifying the set of
	 * finding types that will be in the final group of finding type filters.
	 * 
	 * @param findingTypes
	 *            a mutable set of finding types
	 * @param filterSet
	 *            the filter set to process
	 * @param filterSetMap
	 *            a map of all available filter sets
	 */
	private void processFilterSet(Set<String> findingTypes,
			FilterSet filterSet, Map<String, FilterSet> filterSetMap) {
		final List<String> parents = filterSet.getParent();
		if (parents != null) {
			for (final String parent : parents) {
				processFilterSet(findingTypes, filterSetMap.get(parent),
						filterSetMap);
			}
		}
		for (final FilterEntry fe : filterSet.getFilter()) {
			final String uid = fe.getType();
			if (fe.isFiltered()) {
				findingTypes.remove(uid);
			} else {
				findingTypes.add(uid);
			}
		}
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

	private FilterSetRecord newFilterSetRecord() {
		return new FilterSetRecord(filterSetMapper);
	}

}

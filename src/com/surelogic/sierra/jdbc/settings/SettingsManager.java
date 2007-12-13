package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.FilterSetRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeFilterRecord;
import com.surelogic.sierra.jdbc.record.SettingsRecord;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.FilterEntry;
import com.surelogic.sierra.tool.message.FilterSet;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Settings;

public class SettingsManager {

	/**
	 * Gets the default set of finding type UUIDs that have been selected by
	 * SureLogic to be the default filter set.
	 * 
	 * @return the SureLogic default filter set.
	 */
	public static Set<String> getSureLogicDefaultFilterSet() {
		final Set<String> result = new HashSet<String>();
		result.add("ShortMethodName");
		result.add("ShortVariable");
		return result;
	}

	private static final Logger log = SLLogger
			.getLoggerFor(SettingsManager.class);
	private static final String GLOBAL_NAME = "GLOBAL";
	private static final String GLOBAL_UUID = "de3034ec-65d5-4d4a-b059-1adf8fc7b12d";

	private final FindingTypeManager ftMan;
	private final SettingsRecordFactory factory;

	private final PreparedStatement selectSettingsIdByName;
	private final PreparedStatement selectSettingUids;
	private final PreparedStatement selectSettingProjects;
	private final PreparedStatement selectSettingFilterSets;
	private final PreparedStatement selectFilterSetSettings;
	private final PreparedStatement getSettingsByProject;
	private final PreparedStatement copySettings;
	private final PreparedStatement selectSettingFilters;
	private final PreparedStatement copySettingFilters;
	private final PreparedStatement deleteSettingFilters;
	private final PreparedStatement deleteFilterSetFilters;
	private final PreparedStatement listFilterSetUids;
	private final PreparedStatement loadFilterEntries;
	private final PreparedStatement loadFilterSetParents;
	private final PreparedStatement insertFilterSetParent;
	private final PreparedStatement insertFilterSetEntry;
	private final PreparedStatement selectFilterSetById;
	private final PreparedStatement selectFilterSetChildren;
	private final PreparedStatement selectFilterSetParents;
	private final PreparedStatement deleteFilterSetEntries;
	private final PreparedStatement deleteFilterSetParents;

	private final SettingsProjectManager spManager;

	private SettingsManager(Connection conn) throws SQLException {
		ftMan = FindingTypeManager.getInstance(conn);
		factory = SettingsRecordFactory.getInstance(conn);
		selectSettingsIdByName = conn
				.prepareStatement("SELECT ID FROM SETTINGS WHERE NAME = ?");
		selectSettingUids = conn.prepareStatement("SELECT UUID FROM SETTINGS");
		selectSettingProjects = conn
				.prepareStatement("SELECT PROJECT_NAME FROM SETTINGS_PROJECT_RELTN WHERE SETTINGS_ID = ?");
		selectSettingFilterSets = conn
				.prepareStatement("SELECT FS.UUID FROM SETTING_FILTER_SETS S, FILTER_SET FS WHERE S.SETTINGS_ID = ? AND FS.ID = S.FILTER_SET_ID");
		selectFilterSetSettings = conn
				.prepareStatement("SELECT SFS.SETTINGS_ID, S.UUID FROM SETTING_FILTER_SETS SFS, SETTINGS S WHERE SFS.FILTER_SET_ID = ? AND S.ID = SFS.SETTINGS_ID");
		getSettingsByProject = conn
				.prepareStatement("SELECT S.UUID FROM SETTINGS_PROJECT_RELTN PSR, SETTINGS S WHERE PSR.PROJECT_NAME = ? AND S.ID = PSR.SETTINGS_ID");
		copySettings = conn
				.prepareStatement("INSERT INTO SETTING_FILTER_SETS SELECT ?,FILTER_SET_ID FROM SETTING_FILTER_SETS WHERE SETTINGS_ID = ?");
		selectSettingFilters = conn
				.prepareStatement("SELECT FT.UUID, FTF.DELTA, FTF.IMPORTANCE, FTF.FILTERED FROM SETTING_FILTERS FTF, FINDING_TYPE FT WHERE FTF.SETTINGS_ID = ? AND FT.ID = FTF.FINDING_TYPE_ID");
		copySettingFilters = conn
				.prepareStatement("INSERT INTO SETTING_FILTERS SELECT ?, FINDING_TYPE_ID, DELTA, IMPORTANCE, FILTERED FROM SETTING_FILTERS WHERE SETTINGS_ID = ?");
		deleteSettingFilters = conn
				.prepareStatement("DELETE FROM SETTING_FILTERS WHERE SETTINGS_ID = ?");
		spManager = SettingsProjectManager.getInstance(conn);
		loadFilterEntries = conn
				.prepareStatement("SELECT FT.UUID,FE.FILTERED FROM FILTER_ENTRY FE, FINDING_TYPE FT"
						+ "   WHERE FE.FILTER_SET_ID = ? AND FT.ID = FE.FINDING_TYPE_ID");
		loadFilterSetParents = conn
				.prepareStatement("SELECT PARENT_ID FROM FILTER_SET_RELTN WHERE CHILD_ID = ?");
		listFilterSetUids = conn
				.prepareStatement("SELECT UUID FROM FILTER_SET");
		insertFilterSetParent = conn
				.prepareStatement("INSERT INTO FILTER_SET_RELTN (CHILD_ID,PARENT_ID) VALUES (?,?)");
		insertFilterSetEntry = conn
				.prepareStatement("INSERT INTO FILTER_ENTRY (FILTER_SET_ID,FINDING_TYPE_ID,FILTERED) VALUES (?,?,?)");
		selectFilterSetById = conn
				.prepareStatement("SELECT UUID, NAME FROM FILTER_SET WHERE ID = ?");
		selectFilterSetChildren = conn
				.prepareStatement("SELECT CHILD_ID FROM FILTER_SET_RELTN WHERE PARENT_ID = ?");
		selectFilterSetParents = conn
				.prepareStatement("SELECT PARENT_ID FROM FILTER_SET_RELTN WHERE CHILD_ID = ?");
		deleteFilterSetParents = conn
				.prepareStatement("DELETE FROM FILTER_SET_RELTN WHERE CHILD_ID = ?");
		deleteFilterSetEntries = conn
				.prepareStatement("DELETE FROM FILTER_ENTRY WHERE FILTER_SET_ID = ?");
		deleteFilterSetFilters = conn
				.prepareStatement("DELETE FROM FILTER_SET_FILTERS WHERE FILTER_SET_ID = ?");
	}

	public static SettingsManager getInstance(Connection conn)
			throws SQLException {
		return new SettingsManager(conn);
	}

	/**
	 * 
	 * @param findingTypes
	 *            a collection of finding type uid's that will be filtered out
	 *            by these settings.
	 * @throws SQLException
	 */
	public void writeGlobalSettings(List<FindingTypeFilter> filters)
			throws SQLException {
		final SettingsRecord rec = factory.newSettingsRecord();
		rec.setUid(GLOBAL_UUID);
		if (!rec.select()) {
			rec.setName(GLOBAL_NAME);
			rec.setRevision(0L);
			rec.insert();
		}
		final long settingsId = rec.getId();
		deleteSettingFilters.setLong(1, settingsId);
		deleteSettingFilters.execute();
		applyFilters(factory.newSettingsFilterRecord(), settingsId, filters);
	}

	/**
	 * Write out global settings.
	 * 
	 * @param filterUUIDList
	 *            a list of finding types to be excluded.
	 * @throws SQLException
	 */
	public void writeGlobalSettingsUUID(List<String> filterUUIDList)
			throws SQLException {
		final List<FindingTypeFilter> filters = new ArrayList<FindingTypeFilter>(
				filterUUIDList.size());
		for (String findingType : filterUUIDList) {
			FindingTypeFilter filter = new FindingTypeFilter();
			filter.setName(findingType);
			filter.setFiltered(true);
			filters.add(filter);
		}
		writeGlobalSettings(filters);
	}

	/**
	 * 
	 * @return a collection of finding type uid's that should be filtered out.
	 * @throws SQLException
	 */
	public List<FindingTypeFilter> getGlobalSettings() throws SQLException {
		final SettingsRecord rec = factory.newSettingsRecord();
		rec.setUid(GLOBAL_UUID);
		if (rec.select()) {
			return getSettingFilters(GLOBAL_UUID);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Create a new settings entry.
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public void createSettings(String name, long revision) throws SQLException {
		createSettings(name, null, revision);
	}

	/**
	 * Create a new settings entry, and pre-populate it from existing settings.
	 * 
	 * @param name
	 * @param from
	 *            the name of an existing settings record that we want to copy
	 *            from. May be null.
	 * @throws SQLException
	 */
	public void createSettings(String name, String from, long revision)
			throws SQLException {
		final SettingsRecord record = factory.newSettingsRecord();
		record.setName(name);
		selectSettingsIdByName.setString(1, name);
		final ResultSet set = selectSettingsIdByName.executeQuery();
		try {
			if (set.next()) {
				throw new IllegalArgumentException("Settings with the name "
						+ name + " already exist.");
			} else {
				record.setRevision(revision);
				record.setUid(UUID.randomUUID().toString());
				record.insert();
				if (from != null) {
					SettingsRecord old = factory.newSettingsRecord();
					old.setName(from);
					if (old.select()) {
						copySettings.setLong(1, record.getId());
						copySettings.setLong(2, old.getId());
						copySettings.execute();
						copySettingFilters.setLong(1, record.getId());
						copySettingFilters.setLong(2, old.getId());
						copySettingFilters.execute();
					} else {
						throw new IllegalArgumentException(
								"Settings with the name " + from
										+ " already exist.");
					}
				}
			}
		} finally {
			set.close();
		}
	}

	/**
	 * Change the name of the given settings
	 * 
	 * @param uid
	 * @param name
	 * @throws SQLException
	 */
	public void renameSettings(String uid, String name, long revision)
			throws SQLException {
		SettingsRecord record = factory.newSettingsRecord();
		record.setUid(uid);
		/** Can't rename a setting which does not exist */
		if (record.select()) {
			record.setRevision(revision);
			record.setName(name);
			record.update();
		} else {
			throw new IllegalArgumentException("No settings w/ uid " + uid
					+ " exist.");
		}
	}

	/**
	 * Delete settings entirely.
	 * 
	 * @param settings
	 * @throws SQLException
	 */
	public void deleteSettings(String uid) throws SQLException {
		SettingsRecord record = factory.newSettingsRecord();
		record.setUid(uid);
		/** If this product does not exist, throw an error */
		if (!record.select()) {
			throw new IllegalArgumentException("No settings w/ uid " + uid
					+ " exist.");
		}
		record.delete();
	}

	/**
	 * Retrieve all of the settings that exist in the current database.
	 * 
	 * @return
	 */
	public List<Settings> listSettings() throws SQLException {
		final ResultSet set = selectSettingUids.executeQuery();
		try {
			final List<Settings> settings = new ArrayList<Settings>();
			while (set.next()) {
				settings.add(getSettings(set.getString(1)));
			}
			return settings;
		} finally {
			set.close();
		}

	}

	/**
	 * Retrieve settings by uid.
	 * 
	 * @param uid
	 * @return
	 * @throws SQLException
	 */
	public Settings getSettings(String uid) throws SQLException {
		SettingsRecord rec = factory.newSettingsRecord();
		rec.setUid(uid);
		if (rec.select()) {
			final Settings settings = new Settings();
			selectSettingFilterSets.setLong(1, rec.getId());
			final ResultSet set = selectSettingFilterSets.executeQuery();
			try {
				final List<String> filterSets = settings.getFilterSets();
				while (set.next()) {
					filterSets.add(set.getString(1));
				}
			} finally {
				set.close();
			}
			settings.setName(rec.getName());
			settings.setRevision(rec.getRevision());
			settings.setUid(rec.getUid());
			return settings;
		} else {
			return null;
		}
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
				return getSettings(set.getString(1));
			} else {
				return null;
			}
		} finally {
			set.close();
		}
	}

	/**
	 * Get a list of the project names currently associated with the provided
	 * settings.
	 * 
	 * @param uid
	 * @return
	 * @throws SQLException
	 */
	public List<String> listSettingProjects(String uid) throws SQLException {
		final SettingsRecord rec = factory.newSettingsRecord();
		rec.setUid(uid);
		if (rec.select()) {
			final List<String> projects = new ArrayList<String>();
			selectSettingProjects.setLong(1, rec.getId());
			final ResultSet set = selectSettingProjects.executeQuery();
			try {
				while (set.next()) {
					projects.add(set.getString(1));
				}
			} finally {
				set.close();
			}
			return projects;
		} else {
			throw new IllegalArgumentException("Settings with uid " + uid
					+ " do not exist.");
		}
	}

	/**
	 * Returns a list of the finding types for these settings, and whether they
	 * are on or off.
	 * 
	 * @param uid
	 * @return
	 */
	public List<FindingTypeFilter> getSettingFilters(String uid)
			throws SQLException {
		final SettingsRecord rec = factory.newSettingsRecord();
		rec.setUid(uid);
		if (rec.select()) {
			selectSettingFilters.setLong(1, rec.getId());
			final ResultSet set = selectSettingFilters.executeQuery();
			try {
				final List<FindingTypeFilter> filters = new ArrayList<FindingTypeFilter>();
				while (set.next()) {
					final FindingTypeFilter filter = new FindingTypeFilter();
					int idx = 1;
					filter.setName(set.getString(idx++));
					final int delta = set.getInt(idx++);
					if (!set.wasNull()) {
						filter.setDelta(delta);
					}
					final int importance = set.getInt(idx++);
					if (!set.wasNull()) {
						filter.setImportance(Importance.values()[importance]);
					}
					filter.setFiltered("Y".equals(set.getString(idx++)));
					filters.add(filter);
				}
				return filters;
			} finally {
				set.close();
			}
		} else {
			throw new IllegalArgumentException("No settings with uid " + uid
					+ " exist");
		}
	}

	/**
	 * Add the list of projects to the given settings.
	 * 
	 * @param uid
	 * @param projects
	 * @throws SQLException
	 */
	public void addSettingProjects(String uid, Collection<String> projects)
			throws SQLException {
		SettingsRecord rec = factory.newSettingsRecord();
		rec.setUid(uid);
		if (rec.select()) {
			addProjects(rec, projects);
		} else {
			throw new IllegalArgumentException("Settings with uid " + uid
					+ " do not exist.");
		}
	}

	/**
	 * Remove the list of projects from the given settings.
	 * 
	 * @param uid
	 * @param projects
	 * @throws SQLException
	 */
	public void deleteSettingProjects(String uid, Collection<String> projects)
			throws SQLException {
		SettingsRecord rec = factory.newSettingsRecord();
		rec.setUid(uid);
		if (rec.select()) {
			for (String projectName : projects) {
				spManager.deleteProjectRelation(rec, projectName);
			}
		} else {
			throw new IllegalArgumentException("Settings with uid " + uid
					+ " do not exist.");
		}
	}

	/**
	 * Create an empty filter set.
	 * 
	 * @param name
	 * @param description
	 * @param revision
	 *            an (optional) brief description of the filter set
	 * @return the uid of this filter set
	 * @throws SQLException
	 */
	public String createFilterSet(String name, String description, long revision)
			throws SQLException {
		final FilterSetRecord filterSetRec = factory.newFilterSetRecord();
		filterSetRec.setName(name);
		filterSetRec.setInfo(description);
		filterSetRec.setRevision(revision);
		filterSetRec.setUid(UUID.randomUUID().toString());
		filterSetRec.insert();
		return filterSetRec.getUid();
	}

	/**
	 * Delete an existing filter set.
	 * 
	 * @param uid
	 * @throws SQLException
	 */
	public void deleteFilterSet(String uid) throws SQLException {
		final FilterSetRecord set = factory.newFilterSetRecord();
		set.setUid(uid);
		if (set.select()) {
			set.delete();
		}
		// TODO we need to handle cascade.
	}

	/**
	 * Update an existing filter set. This will overwrite all current
	 * information about the filter set.
	 * 
	 * @param filterSet
	 * @param revision
	 * @throws SQLException
	 */
	public void updateFilterSet(FilterSet filterSet, long revision)
			throws SQLException {
		final FilterSetRecord filterSetRec = factory.newFilterSetRecord();
		filterSetRec.setUid(filterSet.getUid());
		if (filterSetRec.select()) {
			filterSetRec.setRevision(revision);
			filterSetRec.setName(filterSet.getName());
			filterSetRec.update();
			final long filterSetId = filterSetRec.getId();
			deleteFilterSetParents.setLong(1, filterSetId);
			deleteFilterSetParents.execute();
			final FilterSetRecord parentRec = factory.newFilterSetRecord();
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
							+ "was being written, but parent with uid "
							+ parent + " could not be found");
				}
			}
			deleteFilterSetEntries.setLong(1, filterSetId);
			deleteFilterSetEntries.execute();
			for (final FilterEntry entry : filterSet.getFilter()) {
				final long findingTypeId = ftMan.getFindingTypeId(entry
						.getType());
				insertFilterSetEntry.setLong(1, filterSetId);
				insertFilterSetEntry.setLong(2, findingTypeId);
				insertFilterSetEntry.setString(3, entry.isFiltered() ? "Y"
						: "N");
				insertFilterSetEntry.execute();
			}
			// Check for consistency
			getFilterSetParents(filterSetId);
			// Find all settings and other filter sets that depend on this
			// filter set. This could be any setting that points to this filter
			// set, or any of it's children.
			final Set<Long> children = getFilterSetChildren(filterSetId);
			children.add(filterSetId);
			regenerateFilterSetFilters(children);
			for (final long childId : children) {
				selectFilterSetSettings.setLong(1, childId);
				final ResultSet set = selectFilterSetSettings.executeQuery();
				try {
					while (set.next()) {
						final long settingsId = set.getLong(1);
						final Settings settings = getSettings(set.getString(2));
						regenerateSettingFilters(settingsId, settings);
					}
				} finally {
					set.close();
				}
			}
		} else {
			log.warning("No filter set exists with the name "
					+ filterSet.getName() + ", nothing could be updated.");
		}
	}

	/**
	 * 
	 * @param uid
	 * @return
	 * @throws SQLException
	 */
	public FilterSet getFilterSet(final String uid) throws SQLException {
		FilterSetRecord rec = factory.newFilterSetRecord();
		rec.setUid(uid);
		if (rec.select()) {
			return getFilterSetById(rec.getId());
		} else {
			return null;
		}
	}

	private FilterSet getFilterSetById(final long id) throws SQLException {
		final FilterSet filterSet = getFilterSetHelper(id);
		selectFilterSetById.setLong(1, id);
		final ResultSet set = selectFilterSetById.executeQuery();
		if (!set.next()) {
			throw new IllegalArgumentException(id
					+ " is not a valid filter set id.");
		}
		try {
			int idx = 1;
			filterSet.setUid(set.getString(idx++));
			filterSet.setName(set.getString(idx++));
		} finally {
			set.close();
		}
		return filterSet;

	}

	/**
	 * List the filter sets for these settings.
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public List<FilterSetDetail> listSettingFilterSets(String uid)
			throws SQLException {
		Settings settings = getSettings(uid);
		if (settings != null) {
			return getFilterSetDetails(settings.getFilterSets());
		} else {
			throw new IllegalArgumentException("No settings with the uid "
					+ uid + " exist.");
		}
	}

	/**
	 * List all of the filter sets available.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<FilterSetDetail> listFilterSets() throws SQLException {
		final List<String> uids = new ArrayList<String>();
		final ResultSet set = listFilterSetUids.executeQuery();
		try {
			while (set.next()) {
				uids.add(set.getString(1));
			}
		} finally {
			set.close();
		}
		return getFilterSetDetails(uids);
	}

	/**
	 * Find all children of the given filter set.
	 * 
	 * @param parentId
	 * @return
	 * @throws SQLException
	 */
	private Set<Long> getFilterSetChildren(final long parentId)
			throws SQLException {
		final Set<Long> children = new HashSet<Long>();
		getFilterSetChildrenHelper(children, parentId);
		return children;
	}

	private void getFilterSetChildrenHelper(final Set<Long> children,
			long parentId) throws SQLException {
		selectFilterSetChildren.setLong(1, parentId);
		final ResultSet set = selectFilterSetChildren.executeQuery();
		try {
			while (set.next()) {
				final long childId = set.getLong(1);
				if (children.add(childId)) {
					getFilterSetChildrenHelper(children, childId);
				}
			}
		} finally {
			set.close();
		}
	}

	/**
	 * Find all parents of the given filter set.
	 * 
	 * @param childId
	 * @return
	 * @throws SQLException
	 */
	private Set<Long> getFilterSetParents(final long childId)
			throws SQLException {
		final Set<Long> parents = new HashSet<Long>();
		getFilterSetParentsHelper(parents, childId, childId);
		return parents;
	}

	private void getFilterSetParentsHelper(final Set<Long> parents,
			long childId, long initialId) throws SQLException {
		selectFilterSetParents.setLong(1, childId);
		final ResultSet set = selectFilterSetParents.executeQuery();
		try {
			while (set.next()) {
				final long parentId = set.getLong(1);
				if (parentId == initialId) {
					throw new IllegalArgumentException(
							"This filter set contains a cycle, the full set of parents cannot be computed.");
				}
				if (parents.add(parentId)) {
					getFilterSetParentsHelper(parents, parentId, initialId);
				}
			}
		} finally {
			set.close();
		}
	}

	/**
	 * Retrieve the details of a collection of filter set uids.
	 * 
	 * @param uids
	 * @return
	 * @throws SQLException
	 */
	private List<FilterSetDetail> getFilterSetDetails(Collection<String> uids)
			throws SQLException {
		final List<FilterSetDetail> filterSetDetails = new ArrayList<FilterSetDetail>();
		final Map<String, FilterSet> filterSetMap = new HashMap<String, FilterSet>();
		for (String uid : uids) {
			final FilterSet filterSet = getFilterSet(uid);
			filterSetMap.put(filterSet.getUid(), filterSet);
		}
		for (final FilterSet filterSet : filterSetMap.values()) {
			FilterSetDetail detail = new FilterSetDetail();
			detail.setName(filterSet.getName());
			detail.setUid(filterSet.getUid());
			final List<FilterEntryDetail> filterEntryDetails = detail
					.getFilters();
			for (final FilterEntry entry : filterSet.getFilter()) {
				FilterEntryDetail entryDetail = new FilterEntryDetail();
				entryDetail.setFiltered(entry.isFiltered());
				entryDetail.setFindingType(ftMan
						.getFindingType(entry.getType()));
				filterEntryDetails.add(entryDetail);
			}
			final List<ParentDetail> parentDetails = detail.getParents();
			for (final String parent : filterSet.getParent()) {
				ParentDetail parentDetail = new ParentDetail();
				parentDetail.setUid(parent);
				FilterSet parentSet = filterSetMap.get(parent);
				if (parentSet == null) {
					parentSet = getFilterSet(parent);
					filterSetMap.put(parent, parentSet);
				}
				parentDetail.setName(parentSet.getName());
				parentDetails.add(parentDetail);
			}
			filterSetDetails.add(detail);
		}
		return filterSetDetails;
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

	/**
	 * Generates filter set filters according to the following rules:
	 * <ol>
	 * <li>If more than one entry exists for a finding type, the entry closest
	 * to the root filter set takes precedence.
	 * <li>TODO we need to impose an ordering on parents</li>
	 * </ol>
	 * 
	 * @param filterSetId
	 */
	private void regenerateFilterSetFilters(Set<Long> filterSetIds)
			throws SQLException {
		final Map<String, FilterSet> filterSetMap = new HashMap<String, FilterSet>();
		final List<String> filterSetUids = new ArrayList<String>(filterSetIds
				.size());
		final Set<Long> parentIds = new HashSet<Long>();
		for (long id : filterSetIds) {
			deleteFilterSetFilters.setLong(1, id);
			deleteFilterSetFilters.execute();
			final FilterSet filterSet = getFilterSetById(id);
			filterSetMap.put(filterSet.getUid(), filterSet);
			filterSetUids.add(filterSet.getUid());
			parentIds.addAll(getFilterSetParents(id));
		}
		parentIds.addAll(filterSetIds);
		for (long id : parentIds) {
			final FilterSet filterSet = getFilterSetById(id);
			filterSetMap.put(filterSet.getUid(), filterSet);
		}
		final Iterator<Long> filterSetIter = filterSetIds.iterator();
		for (String uid : filterSetUids) {
			final Set<String> findingTypes = new HashSet<String>();
			processFilterSet(findingTypes, filterSetMap.get(uid), filterSetMap);
			final List<FindingTypeFilter> findingTypeFilters = new ArrayList<FindingTypeFilter>();
			for (String type : findingTypes) {
				final FindingTypeFilter ftf = new FindingTypeFilter();
				ftf.setFiltered(false);
				ftf.setName(type);
				findingTypeFilters.add(ftf);
			}
			applyFilters(factory.newFilterSetFilterRecord(), filterSetIter
					.next(), findingTypeFilters);
		}
	}

	/**
	 * Generates the setting finding type filters based off of the filter sets
	 * associated w/ these settings. This should for settings when they are
	 * updated, or when a filter set has changed.
	 * 
	 * @param settingsId
	 * @throws SQLException
	 */
	private void regenerateSettingFilters(final long settingsId,
			final Settings settings) throws SQLException {
		deleteSettingFilters.setLong(1, settingsId);
		deleteSettingFilters.execute();
		final Map<String, FilterSet> filterSetMap = new HashMap<String, FilterSet>();
		final List<FilterSet> filterSets = new ArrayList<FilterSet>();
		for (String uid : settings.getFilterSets()) {
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
		applyFilters(factory.newSettingsFilterRecord(), settingsId,
				findingTypeFilters);
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

	private void addProjects(SettingsRecord settings,
			Collection<String> projects) throws SQLException {
		if (projects != null) {
			for (String projectName : projects) {
				spManager.addRelation(settings, projectName);
			}
		}
	}

	/**
	 * Write the given filters out to the databse.
	 * 
	 * @param rec
	 * @param entityId
	 * @param filters
	 * @throws SQLException
	 */
	private void applyFilters(FindingTypeFilterRecord rec, Long entityId,
			List<FindingTypeFilter> filters) throws SQLException {
		for (FindingTypeFilter filter : filters) {
			final Long findingTypeId = ftMan.getFindingTypeId(filter.getName());
			if (findingTypeId != null) {
				rec.setId(new FindingTypeFilterRecord.PK(entityId,
						findingTypeId));
				rec.setImportance(filter.getImportance());
				rec.setFiltered(filter.isFiltered());
				rec.setDelta(filter.getDelta());
				rec.insert();
			} else {
				throw new IllegalArgumentException(filter.getName()
						+ " is not a valid filter name.");
			}
		}
	}

}

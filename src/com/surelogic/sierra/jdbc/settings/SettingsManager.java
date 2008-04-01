package com.surelogic.sierra.jdbc.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.FindingTypeFilterRecord;
import com.surelogic.sierra.jdbc.record.SettingsRecord;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Settings;

public final class SettingsManager {

	/**
	 * The full location, on the Java classpath, of the default world file.
	 */
	public static final String DEFAULT_FILTER_SET_FILE = "/com/surelogic/sierra/jdbc/settings/SureLogicDefaultFilterSet.txt";

	/**
	 * Gets the default set of finding type UUIDs that have been selected by
	 * SureLogic to be the default filter set.
	 * 
	 * @return the SureLogic default filter set.
	 */
	public static Set<String> getSureLogicDefaultFilterSet() {
		final Set<String> result = new HashSet<String>();

		final URL defaultURL = Thread.currentThread().getContextClassLoader()
				.getResource(DEFAULT_FILTER_SET_FILE);
		if (defaultURL == null) {
			SLLogger.getLogger().log(
					Level.WARNING,
					"Unable to find the SureLogic default filter set file "
							+ DEFAULT_FILTER_SET_FILE + " on the classpath.");
			return result;
		}
		try {
			final BufferedReader in = new BufferedReader(new InputStreamReader(
					defaultURL.openStream()));
			try {
				String s = in.readLine();
				while (s != null) {
					s = s.trim();
					if (!s.startsWith("--") && !"".equals(s)) {
						result.add(s);
					}
					s = in.readLine();
				}
			} finally {
				in.close();
			}
		} catch (final IOException e) {
			SLLogger.getLogger().log(
					Level.WARNING,
					"IO failure reading the SureLogic default filter set file "
							+ DEFAULT_FILTER_SET_FILE, e);
		}
		return result;
	}

	private static final String GLOBAL_NAME = "GLOBAL";
	private static final String GLOBAL_UUID = "de3034ec-65d5-4d4a-b059-1adf8fc7b12d";

	private final FindingTypeManager ftMan;
	private final SettingsRecordFactory factory;

	private final PreparedStatement selectSettingsIdByName;
	private final PreparedStatement selectSettingUids;
	private final PreparedStatement selectSettingProjects;
	private final PreparedStatement selectSettingFilterSets;
	private final PreparedStatement getSettingsByProject;
	private final PreparedStatement copySettings;
	private final PreparedStatement selectSettingFilters;
	private final PreparedStatement copySettingFilters;
	private final PreparedStatement deleteSettingFilters;

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
		for (final String findingType : filterUUIDList) {
			final FindingTypeFilter filter = new FindingTypeFilter();
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

	public Set<String> getGlobalSettingsUUID() throws SQLException {
		final Set<String> result = new HashSet<String>();
		for (final FindingTypeFilter filter : getGlobalSettings()) {
			/*
			 * The name is actually the UUID.
			 */
			result.add(filter.getName());
		}
		return result;
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
					final SettingsRecord old = factory.newSettingsRecord();
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
		final SettingsRecord record = factory.newSettingsRecord();
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
		final SettingsRecord record = factory.newSettingsRecord();
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
		final SettingsRecord rec = factory.newSettingsRecord();
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
		final ResultSet set = getSettingsByProject.executeQuery();
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
		final SettingsRecord rec = factory.newSettingsRecord();
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
		final SettingsRecord rec = factory.newSettingsRecord();
		rec.setUid(uid);
		if (rec.select()) {
			for (final String projectName : projects) {
				spManager.deleteProjectRelation(rec, projectName);
			}
		} else {
			throw new IllegalArgumentException("Settings with uid " + uid
					+ " do not exist.");
		}
	}

	// TODO restore the filter set code that was here

	private void addProjects(SettingsRecord settings,
			Collection<String> projects) throws SQLException {
		if (projects != null) {
			for (final String projectName : projects) {
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
		for (final FindingTypeFilter filter : filters) {
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

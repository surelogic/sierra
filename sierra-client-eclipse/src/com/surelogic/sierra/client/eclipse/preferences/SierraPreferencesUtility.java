package com.surelogic.sierra.client.eclipse.preferences;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import com.surelogic.common.FileUtility;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.preferences.AutoPerspectiveSwitchPreferences;
import com.surelogic.sierra.client.eclipse.views.ServerStatusSort;
import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.message.Importance;

/**
 * Defines preference constants for the Sierra tool.
 * <p>
 * The preferences are manipulated using the API in {@link EclipseUtility}.
 * Eclipse UI code that uses an <tt>IPreferenceStore</tt> may obtain one that
 * accesses the Sierra preferences by calling
 * <tt>EclipseUIUtility.getPreferences()</tt>.
 */
public final class SierraPreferencesUtility {

	private static final String PREFIX = "com.surelogic.sierra.";

	private static final AtomicBoolean f_initializationNeeded = new AtomicBoolean(
			true);

	/**
	 * Sets up the default values for the Sierra tool.
	 * <p>
	 * <b>WARNING:</b> Because this class exports strings that are declared to
	 * be {@code public static final} simply referencing these constants may not
	 * trigger Eclipse to load the containing plug-in. This is because the
	 * constants are copied by the Java compiler into using class files. This
	 * means that each using plug-in <b>must</b> invoke
	 * {@link #initializeDefaultScope()} in its plug-in activator's {@code
	 * start} method.
	 */
	public static void initializeDefaultScope() {
		if (f_initializationNeeded.compareAndSet(true, false)) {
			EclipseUtility.setDefaultBooleanPreference(
					SHOW_BALLOON_NOTIFICATIONS, true);
			EclipseUtility.setDefaultBooleanPreference(getSwitchPreferences()
					.getAutoPerspectiveSwitchConstant(), true);
			EclipseUtility.setDefaultBooleanPreference(getSwitchPreferences()
					.getPromptPerspectiveSwitchConstant(), true);
			EclipseUtility.setDefaultStringPreference(
					SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE, Importance.HIGH
							.toString());
			EclipseUtility.setDefaultBooleanPreference(ALWAYS_SAVE_RESOURCES,
					false);
			EclipseUtility.setDefaultBooleanPreference(SHOW_MARKERS, true);
			EclipseUtility.setDefaultBooleanPreference(SHOW_JSURE_FINDINGS,
					false);
			EclipseUtility.setDefaultIntPreference(FINDINGS_LIST_LIMIT, 2000);
			EclipseUtility.setDefaultIntPreference(TOOL_MEMORY_MB, 1024);
			EclipseUtility.setDefaultBooleanPreference(
					WARN_MAXIMUM_FINDINGS_SHOWN, true);
			EclipseUtility.setDefaultBooleanPreference(
					ALWAYS_ALLOW_USER_TO_SELECT_PROJECTS_TO_SCAN, true);
			EclipseUtility.setDefaultStringPreference(SERVER_STATUS_SORT,
					ServerStatusSort.BY_SERVER.toString());
			EclipseUtility.setDefaultIntPreference(
					SERVER_INTERACTION_PERIOD_IN_MINUTES, 5);
			EclipseUtility.setDefaultIntPreference(
					SERVER_INTERACTION_AUDIT_THRESHOLD, 200);
			EclipseUtility.setDefaultIntPreference(
					SERVER_INTERACTION_RETRY_THRESHOLD, 4);
			EclipseUtility.setDefaultStringPreference(SERVER_FAILURE_REPORTING,
					ServerFailureReport.SHOW_BALLOON.toString());
			EclipseUtility
					.setDefaultStringPreference(
							SIERRA_DATA_DIRECTORY,
							EclipseUtility
									.getADataDirectoryPath(FileUtility.SIERRA_DATA_PATH_FRAGMENT));
			/*
			 * We'll take the default-default for the other preferences.
			 */
		}
	}

	public static final String ALWAYS_SAVE_RESOURCES = PREFIX
			+ "always-save-resources";
	public static final String DELETE_DB_ON_STARTUP = PREFIX
			+ "delete-db-on-startup";
	public static final String SHOW_BALLOON_NOTIFICATIONS = PREFIX
			+ "show-balloon-notifications";
	public static final String SHOW_MARKERS = PREFIX + "show-markers";
	public static final String SHOW_JSURE_FINDINGS = PREFIX
			+ "show-jsure-findings";
	public static final String SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE = PREFIX
			+ "show-markers-at-or-above-importance";
	public static final String FINDINGS_LIST_LIMIT = PREFIX
			+ "findings-list-limit";
	public static final String TOOL_MEMORY_MB = PREFIX + "tool-memory-mb";
	public static final String WARN_MAXIMUM_FINDINGS_SHOWN = PREFIX
			+ "warn-maximum-findings-shown";
	public static final String ALWAYS_ALLOW_USER_TO_SELECT_PROJECTS_TO_SCAN = PREFIX
			+ "always-allow-user-to-select-projects-to-scan";
	public static final String LAST_TIME_PROJECTS_TO_SCAN = PREFIX
			+ "last-time-projects-to-scan";
	public static final String LAST_TIME_PROJECTS_TO_PUBLISH = PREFIX
			+ "last-time-projects-to-publish";
	public static final String LAST_TIME_PROJECTS_TO_SYNC = PREFIX
			+ "last-time-projects-to-sync";
	public static final String HIDE_EMPTY_SYNCHRONIZE_ENTRIES = PREFIX
			+ "hide-empty-synchronize-entries";
	public static final String SERVER_STATUS_SORT = PREFIX
			+ "server-status-sort";

	public static ServerStatusSort getServerStatusSort() {
		final String sort = EclipseUtility
				.getStringPreference(SERVER_STATUS_SORT);
		return ServerStatusSort.valueOf(sort);
	}

	public static void setServerStatusSort(final ServerStatusSort value) {
		EclipseUtility
				.setStringPreference(SERVER_STATUS_SORT, value.toString());
	}

	/**
	 * Period in minutes of automatic server interaction.
	 */
	public static final String SERVER_INTERACTION_PERIOD_IN_MINUTES = PREFIX
			+ "server-interaction-period-in-minutes";
	/**
	 * Threshold of # of unsynchronized audits for triggering a sync.
	 */
	public static final String SERVER_INTERACTION_AUDIT_THRESHOLD = PREFIX
			+ "server-interaction-audit-threshold";
	public static final String SERVER_INTERACTION_RETRY_THRESHOLD = PREFIX
			+ "server-interaction-retry-limit";

	/**
	 * Setting to control reporting of failures during automatic synchronization
	 */
	public static final String SERVER_FAILURE_REPORTING = PREFIX
			+ "server-failure-reporting";

	public static ServerFailureReport getServerFailureReporting() {
		final String resultString = EclipseUtility
				.getStringPreference(SERVER_FAILURE_REPORTING);
		final ServerFailureReport result = ServerFailureReport
				.valueOf(resultString);
		return result;
	}

	public static void getServerFailureReporting(final ServerFailureReport s) {
		EclipseUtility.setStringPreference(SERVER_FAILURE_REPORTING, s
				.toString());
	}

	public static final String getToolPreferenceConstant(final IToolFactory f) {
		return PREFIX + "run-" + f.getId();
	}

	public static boolean runTool(final IToolFactory f) {
		return EclipseUtility
				.getBooleanPreference(getToolPreferenceConstant(f));
	}

	public static Importance showMarkersAtOrAboveImportance() {
		final String resultString = EclipseUtility
				.getStringPreference(SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE);
		final Importance result = Importance.fromValue(resultString);
		return result;
	}

	private static final String SIERRA_DATA_DIRECTORY = PREFIX
			+ "data.directory";

	/**
	 * Gets the Sierra data directory. This method ensures that the directory
	 * does exist on the disk. It checks that is is there and, if not, tries to
	 * create it. If it can't be created the method throws an exception.
	 * 
	 * @return the Sierra data directory.
	 * 
	 * @throws IllegalStateException
	 *             if the Sierra data directory doesn't exist on the disk and
	 *             can't be created.
	 */
	public static File getSierraDataDirectory() {
		final String path = EclipseUtility
				.getStringPreference(SIERRA_DATA_DIRECTORY);
		final File result = new File(path);
		FileUtility.ensureDirectoryExists(path);
		return result;
	}

	/**
	 * Sets the Sierra data directory to an existing directory.
	 * <p>
	 * This method simply changes the preference it does not move data from the
	 * old directory (or even delete the old directory).
	 * 
	 * @param dir
	 *            the new Sierra data directory (must exist and be a directory).
	 * 
	 * @throws IllegalArgumentException
	 *             if the passed {@link File} is not a directory or doesn't
	 *             exist.
	 */
	public static void setSierraDataDirectory(final File dir) {
		if (dir != null && dir.isDirectory()) {
			EclipseUtility.setStringPreference(SIERRA_DATA_DIRECTORY, dir
					.getAbsolutePath());
		} else {
			throw new IllegalArgumentException("Bad Sierra data directory "
					+ dir + " it doesn't exist on the disk");
		}
	}

	/**
	 * Gets the switch-to-the-Sierra-perspective preferences.
	 * 
	 * @return the switch-to-the-Sierra-perspective preferences.
	 */
	public static AutoPerspectiveSwitchPreferences getSwitchPreferences() {
		return new AutoPerspectiveSwitchPreferences() {
			@Override
			public String getConstant(String suffix) {
				return PREFIX + suffix;
			}
		};
	}

	private SierraPreferencesUtility() {
		// utility
	}
}
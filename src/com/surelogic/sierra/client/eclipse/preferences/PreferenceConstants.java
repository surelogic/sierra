package com.surelogic.sierra.client.eclipse.preferences;

import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.views.ServerStatusSort;
import com.surelogic.sierra.tool.message.Importance;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	private static final String PREFIX = "com.surelogic.sierra.";

	public static final String P_SIERRA_ALWAYS_SAVE_RESOURCES = PREFIX
			+ "always-save-resources";

	public static boolean alwaysSaveResources() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_ALWAYS_SAVE_RESOURCES);
	}

	public static final String P_SIERRA_DELETE_DB_ON_STARTUP = PREFIX
			+ "delete-db-on-startup";

	public static boolean deleteDatabaseOnStartup() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_DELETE_DB_ON_STARTUP);
	}

	public static void setDeleteDatabaseOnStartup(
			boolean deleteDatabaseOnStartup) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SIERRA_DELETE_DB_ON_STARTUP, deleteDatabaseOnStartup);
	}

	public static final String P_SIERRA_BALLOON_FLAG = PREFIX + "balloon-flag";

	public static boolean showBalloonNotifications() {
		if (Activator.getDefault() == null) {
			return false;
		}
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_BALLOON_FLAG);
	}

	public static final String P_RUN_FINDBUGS = PREFIX + "run-FindBugs";

	public static boolean runFindBugs() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_RUN_FINDBUGS);
	}

	public static final String P_RUN_PMD = PREFIX + "run-PMD";

	public static boolean runPMD() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_RUN_PMD);
	}

	public static final String P_RUN_RECKONER = PREFIX + "run-Reckoner";

	public static boolean runReckoner() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_RUN_RECKONER);
	}

	public static final String P_SIERRA_SHOW_MARKERS = PREFIX + "show-markers";

	public static boolean showMarkers() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_SHOW_MARKERS);
	}

	public static void setMarkersVisibility(boolean visible) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SIERRA_SHOW_MARKERS, visible);
	}

	public static final String P_SIERRA_SHOW_JSURE_FINDINGS = PREFIX
			+ "show-jsure-findings";

	public static boolean showJSureFindings() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_SHOW_JSURE_FINDINGS);
	}

	public static void setJSureFindingsVisibility(boolean visible) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SIERRA_SHOW_JSURE_FINDINGS, visible);
	}

	public static final String P_SIERRA_SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE = PREFIX
			+ "show-markers-at-or-above-importance";

	public static Importance showMarkersAtOrAboveImportance() {
		final String resultString = Activator.getDefault()
				.getPluginPreferences().getString(
						P_SIERRA_SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE);
		final Importance result = Importance.fromValue(resultString);
		return result;
	}

	public static final String P_FINDINGS_LIST_LIMIT = PREFIX
			+ "findings-list-limit";

	public static int getFindingsListLimit() {
		return Activator.getDefault().getPluginPreferences().getInt(
				P_FINDINGS_LIST_LIMIT);
	}

	public static final String P_TOOL_MEMORY_MB = PREFIX + "tool-memory-mb";

	public static int getToolMemoryMB() {
		return Activator.getDefault().getPluginPreferences().getInt(
				P_TOOL_MEMORY_MB);
	}

	public static void setToolMemoryMB(int mb) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_TOOL_MEMORY_MB, mb);
	}

	public static final String P_WARN_MAXIMUM_FINDINGS_SHOWN = PREFIX
			+ "warn-maximum-findings-shown";

	public static boolean warnAboutMaximumFindingsShown() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_WARN_MAXIMUM_FINDINGS_SHOWN);
	}

	public static void setWarnAboutMaximumFindingsShown(boolean value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_WARN_MAXIMUM_FINDINGS_SHOWN, value);
	}

	public static final String P_SELECT_PROJECTS_TO_SCAN = PREFIX
			+ "always-allow-user-to-select-projects-to-scan";

	public static boolean alwaysAllowUserToSelectProjectsToScan() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SELECT_PROJECTS_TO_SCAN);
	}

	public static void setAlwaysAllowUserToSelectProjectsToScan(boolean value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SELECT_PROJECTS_TO_SCAN, value);
	}

	public static final String P_OMIT_EMPTY_SYNCHRONIZE_ENTRIES = PREFIX
			+ "omit-empty-synchronize-entries";

	public static boolean hideEmptySynchronizeEntries() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_OMIT_EMPTY_SYNCHRONIZE_ENTRIES);
	}

	public static void setHideEmptySynchronizeEntries(boolean value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_OMIT_EMPTY_SYNCHRONIZE_ENTRIES, value);
	}

	public static final String P_SERVER_STATUS_SORT = PREFIX
			+ "server-status-sort";

	public static ServerStatusSort getServerStatusSort() {
		String sort = Activator.getDefault().getPluginPreferences().getString(
				P_SERVER_STATUS_SORT);
		return ServerStatusSort.valueOf(sort);
	}

	public static void setServerStatusSort(ServerStatusSort value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SERVER_STATUS_SORT, value.toString());
	}

	/**
	 * Period in minutes of automatic server interaction.
	 */
	public static final String P_SERVER_INTERACTION_PERIOD_IN_MINUTES = PREFIX
			+ "server-interaction-period-in-minutes";

	public static int getServerInteractionPeriodInMinutes() {
		if (Activator.getDefault() == null) {
			return 0;
		}
		return Activator.getDefault().getPluginPreferences().getInt(
				P_SERVER_INTERACTION_PERIOD_IN_MINUTES);
	}

	public static void setServerInteractionPeriodInMinutes(int value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SERVER_INTERACTION_PERIOD_IN_MINUTES, value);
	}

	/**
	 * Threshold of # of unsynchronized audits for triggering a sync.
	 */
	public static final String P_SERVER_INTERACTION_AUDIT_THRESHOLD = PREFIX
			+ "server-interaction-audit-threshold";

	public static int getServerInteractionAuditThreshold() {
		return Activator.getDefault().getPluginPreferences().getInt(
				P_SERVER_INTERACTION_AUDIT_THRESHOLD);
	}

	public static void setServerInteractionAuditThreshold(int value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SERVER_INTERACTION_AUDIT_THRESHOLD, value);
	}

	public static final String P_SERVER_INTERACTION_RETRY_THRESHOLD = PREFIX
			+ "server-interaction-retry-limit";

	public static int getServerInteractionRetryThreshold() {
		return Activator.getDefault().getPluginPreferences().getInt(
				P_SERVER_INTERACTION_RETRY_THRESHOLD);
	}

	public static void setServerInteractionRetryThreshold(int value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SERVER_INTERACTION_RETRY_THRESHOLD, value);
	}

	/**
	 * Setting to control reporting of failures during automatic synchronization
	 */
	public static final String P_SERVER_FAILURE_REPORTING = PREFIX
			+ "server-failure-reporting";

	public static ServerFailureReport getServerFailureReporting() {
		final String resultString = Activator.getDefault()
				.getPluginPreferences().getString(P_SERVER_FAILURE_REPORTING);
		final ServerFailureReport result = ServerFailureReport
				.valueOf(resultString);
		return result;
	}

	public static void getServerFailureReporting(ServerFailureReport s) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SERVER_FAILURE_REPORTING, s.toString());
	}

	public static final String P_PROMPT_PERSPECTIVE_SWITCH = PREFIX
			+ "perspective-switch-prompt";

	public static boolean getPromptForPerspectiveSwitch() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_PROMPT_PERSPECTIVE_SWITCH);
	}

	public static void setPromptForPerspectiveSwitch(boolean value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_PROMPT_PERSPECTIVE_SWITCH, value);
	}

	public static final String P_AUTO_PERSPECTIVE_SWITCH = PREFIX
			+ "perspective.switch.auto";

	public static boolean getAutoPerspectiveSwitch() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_AUTO_PERSPECTIVE_SWITCH);
	}

	public static void setAutoPerspectiveSwitch(boolean value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_AUTO_PERSPECTIVE_SWITCH, value);
	}

}

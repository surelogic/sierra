package com.surelogic.sierra.client.eclipse.preferences;

import com.surelogic.common.XUtil;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.tool.message.Importance;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	public static final String P_SIERRA_ALWAYS_SAVE_RESOURCES = "com.surelogic.sierra.always-save-resources";

	public static boolean alwaysSaveResources() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_ALWAYS_SAVE_RESOURCES);
	}

	public static final String P_SIERRA_DELETE_DB_ON_STARTUP = "com.surelogic.sierra.delete-db-on-startup";

	public static boolean deleteDatabaseOnStartup() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_DELETE_DB_ON_STARTUP);
	}

	public static void setDeleteDatabaseOnStartup(
			boolean deleteDatabaseOnStartup) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SIERRA_DELETE_DB_ON_STARTUP, deleteDatabaseOnStartup);
	}

	public static final String P_SIERRA_BALLOON_FLAG = "com.surelogic.sierra.balloon-flag";

	public static boolean showBalloonNotifications() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_BALLOON_FLAG);
	}

	public static final String P_RUN_FINDBUGS = "com.surelogic.sierra.runFindBugs";

	public static boolean runFindBugs() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_RUN_FINDBUGS);
	}

	public static final String P_RUN_PMD = "com.surelogic.sierra.runPMD";

	public static boolean runPMD() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_RUN_PMD);
	}

	public static final String P_RUN_RECKONER = "com.surelogic.sierra.runReckoner";

	public static boolean runReckoner() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_RUN_RECKONER);
	}

	public static final String P_RUN_CHECKSTYLE = "com.surelogic.sierra.runCheckStyle";

	public static boolean runCheckStyle() {
		if (XUtil.useExperimental()) {
			return Activator.getDefault().getPluginPreferences().getBoolean(
					P_RUN_CHECKSTYLE);
		} else {
			return false;
		}
	}

	public static final String P_SIERRA_SHOW_MARKERS = "com.surelogic.sierra.show-markers";

	public static boolean showMarkers() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_SHOW_MARKERS);
	}

	public static void setMarkersVisibility(boolean visible) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SIERRA_SHOW_MARKERS, visible);
	}

	public static final String P_SIERRA_SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE = "com.surelogic.sierra.show-markers-at-or-above-importance";

	public static Importance showMarkersAtOrAboveImportance() {
		final String resultString = Activator.getDefault()
				.getPluginPreferences().getString(
						P_SIERRA_SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE);
		final Importance result = Importance.fromValue(resultString);
		return result;
	}

	public static final String P_FINDINGS_LIST_LIMIT = "com.surelogic.sierra.findings-list-limit";

	public static int getFindingsListLimit() {
		return Activator.getDefault().getPluginPreferences().getInt(
				P_FINDINGS_LIST_LIMIT);
	}

	public static final String P_TOOL_MEMORY_MB = "com.surelogic.sierra.tool-memory-mb";

	public static int getToolMemoryMB() {
		return Activator.getDefault().getPluginPreferences().getInt(
				P_TOOL_MEMORY_MB);
	}

	public static final String P_WARN_MAXIMUM_FINDINGS_SHOWN = "com.surelogic.common.eclipse.warn-maximum-findings-shown";

	public static boolean warnAboutMaximumFindingsShown() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_WARN_MAXIMUM_FINDINGS_SHOWN);
	}

	public static void setWarnAboutMaximumFindingsShown(boolean value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_WARN_MAXIMUM_FINDINGS_SHOWN, value);
	}
}

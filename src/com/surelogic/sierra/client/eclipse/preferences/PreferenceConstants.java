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

	public static final String P_SIERRA_PATH = "com.surelogic.sierra.path";

	public static String getSierraPath() {
		return Activator.getDefault().getPluginPreferences().getString(
				P_SIERRA_PATH);
	}

	public static final String P_SIERRA_BALLOON_FLAG = "com.surelogic.sierra.balloon-flag";

	public static boolean showBalloonNotifications() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_BALLOON_FLAG);
	}

	public static final String P_SIERRA_SHOW_LOWEST_FLAG = "com.surelogic.sierra.show-lowest-importance-flag";

	@Deprecated
	public static boolean showLowestImportance() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_SHOW_LOWEST_FLAG);
	}

	public static Importance showMarkersAtOrAboveImportance() {
		return Importance.HIGH;
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

	public static final String P_TOGGLE_MARKERS = "com.surelogic.sierra.toggle-markers";

	public static boolean showMarkers() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_TOGGLE_MARKERS);
	}

	public static void setMarkersVisibility(boolean visible) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_TOGGLE_MARKERS, visible);
	}
}

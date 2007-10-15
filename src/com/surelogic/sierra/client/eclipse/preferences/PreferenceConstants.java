package com.surelogic.sierra.client.eclipse.preferences;

import com.surelogic.common.XUtil;
import com.surelogic.sierra.client.eclipse.Activator;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

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

	public static boolean showLowestImportance() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SIERRA_SHOW_LOWEST_FLAG);
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
}

package com.surelogic.sierra.client.eclipse.preferences;

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
}

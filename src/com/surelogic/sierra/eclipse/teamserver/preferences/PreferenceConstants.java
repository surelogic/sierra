package com.surelogic.sierra.eclipse.teamserver.preferences;

import com.surelogic.sierra.eclipse.teamserver.Activator;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	public static final String P_SERVER_STAYS_RUNNING = "com.surelogic.sierra.eclipse.teamserver.staysRunning";

	public static boolean warnAboutServerStaysRunning() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_SERVER_STAYS_RUNNING);
	}

	public static void setWarnAboutServerStaysRunning(boolean value) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SERVER_STAYS_RUNNING, value);
	}
}

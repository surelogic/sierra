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

	/*
	 * Values: 0 = Jetty log; 1 = Portal log; 2 = Services log.
	 */
	public static final String P_LOG_SHOWING = "com.surelogic.sierra.eclipse.teamserver.logShowning";

	public static int getLogShowing() {
		return Activator.getDefault().getPluginPreferences().getInt(
				P_LOG_SHOWING);
	}

	public static void setLogShowing(int value) {
		Activator.getDefault().getPluginPreferences().setValue(P_LOG_SHOWING,
				value);
	}
}

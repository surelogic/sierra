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

	public static final String P_LOG_VISIBLE = "com.surelogic.sierra.eclipse.teamserver.logVisible";

	public static boolean isLogVisible() {
		return Activator.getDefault().getPluginPreferences().getBoolean(
				P_LOG_VISIBLE);
	}

	public static void setLogVisible(boolean value) {
		Activator.getDefault().getPluginPreferences().setValue(P_LOG_VISIBLE,
				value);
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

	public static final String P_PORT = "com.surelogic.sierra.eclipse.teamserver.port";

	public static int getPort() {
		return Activator.getDefault().getPluginPreferences().getInt(P_PORT);
	}

	public static void setPort(int value) {
		Activator.getDefault().getPluginPreferences().setValue(P_PORT, value);
	}

	public static final String P_SERVER_MEMORY_MB = "com.surelogic.sierra.teamserver.server-memory-mb";

	public static int getServerMemoryMB() {
		return Activator.getDefault().getPluginPreferences().getInt(
				P_SERVER_MEMORY_MB);
	}

	public static void setServerMemoryMB(int mb) {
		Activator.getDefault().getPluginPreferences().setValue(
				P_SERVER_MEMORY_MB, mb);
	}
}

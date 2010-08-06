package com.surelogic.sierra.eclipse.teamserver.preferences;

import java.io.File;
import java.util.logging.Level;

import com.surelogic.common.FileUtility;
import com.surelogic.sierra.eclipse.teamserver.Activator;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	public static final String P_SERVER_STAYS_RUNNING = "com.surelogic.sierra.eclipse.teamserver.staysRunning";

	public static boolean warnAboutServerStaysRunning() {
		return Activator.getDefault().getPreferenceStore().getBoolean(
				P_SERVER_STAYS_RUNNING);
	}

	public static void setWarnAboutServerStaysRunning(boolean value) {
		Activator.getDefault().getPreferenceStore().setValue(
				P_SERVER_STAYS_RUNNING, value);
	}

	public static final String P_LOG_VISIBLE = "com.surelogic.sierra.eclipse.teamserver.logVisible";

	public static boolean isLogVisible() {
		return Activator.getDefault().getPreferenceStore().getBoolean(
				P_LOG_VISIBLE);
	}

	public static void setLogVisible(boolean value) {
		Activator.getDefault().getPreferenceStore().setValue(P_LOG_VISIBLE,
				value);
	}

	/*
	 * Values: 0 = Jetty console log; 1 = Jetty request log.
	 */
	public static final String P_LOG_SHOWING = "com.surelogic.sierra.eclipse.teamserver.logShowning";

	/**
	 * The log that is showing.
	 * 
	 * @return 0 for Jetty console log, 1 for Jetty request log.
	 */
	public static int getLogShowing() {
		return Activator.getDefault().getPreferenceStore()
				.getInt(P_LOG_SHOWING);
	}

	public static void setLogShowing(int value) {
		Activator.getDefault().getPreferenceStore().setValue(P_LOG_SHOWING,
				value);
	}

	public static final String P_PORT = "com.surelogic.sierra.eclipse.teamserver.port";

	public static int getPort() {
		return Activator.getDefault().getPreferenceStore().getInt(P_PORT);
	}

	public static void setPort(int value) {
		Activator.getDefault().getPreferenceStore().setValue(P_PORT, value);
	}

	public static final String P_SERVER_MEMORY_MB = "com.surelogic.sierra.teamserver.server-memory-mb";

	public static int getServerMemoryMB() {
		return Activator.getDefault().getPreferenceStore().getInt(
				P_SERVER_MEMORY_MB);
	}

	public static void setServerMemoryMB(int mb) {
		Activator.getDefault().getPreferenceStore().setValue(
				P_SERVER_MEMORY_MB, mb);
	}

	public static final String P_SERVER_LOGGING_LEVEL = "com.surelogic.sierra.teamserver.server-logging-level";

	public static Level getServerLoggingLevel() {
		final String level = Activator.getDefault().getPreferenceStore()
				.getString(P_SERVER_LOGGING_LEVEL);
		return Level.parse(level);
	}

	public static File getSierraLocalTeamServerDirectory() {
		final File dataDir = com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants
				.getSierraDataDirectory();
		final File teamServerDir = new File(dataDir,
				FileUtility.LOCAL_TEAM_SERVER_PATH_FRAGMENT);
		FileUtility.createDirectory(teamServerDir);
		return teamServerDir;
	}
}

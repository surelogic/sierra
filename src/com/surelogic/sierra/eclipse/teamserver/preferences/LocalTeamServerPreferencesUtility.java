package com.surelogic.sierra.eclipse.teamserver.preferences;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import com.surelogic.common.FileUtility;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;

public final class LocalTeamServerPreferencesUtility {

	private static final String PREFIX = "com.surelogic.sierra.eclipse.teamserver.";

	private static final AtomicBoolean f_initializationNeeded = new AtomicBoolean(
			true);

	public static void initializeDefaultScope() {
		if (f_initializationNeeded.compareAndSet(true, false)) {
			EclipseUtility.setDefaultBooleanPreference(SERVER_STAYS_RUNNING,
					true);
			EclipseUtility.setDefaultIntPreference(PORT, 13376);
			EclipseUtility.setDefaultIntPreference(SERVER_MEMORY_MB, 1024);
			EclipseUtility.setDefaultStringPreference(SERVER_LOGGING_LEVEL,
					Level.INFO.toString());
			/*
			 * We'll take the default-default for the other preferences.
			 */
		}
	}

	private static final String SERVER_STAYS_RUNNING = PREFIX + "stays-running";

	public static boolean warnAboutServerStaysRunning() {
		return EclipseUtility.getBooleanPreference(SERVER_STAYS_RUNNING);
	}

	public static void setWarnAboutServerStaysRunning(boolean value) {
		EclipseUtility.setBooleanPreference(SERVER_STAYS_RUNNING, value);
	}

	public static final String LOG_VISIBLE = PREFIX + "log-visible";

	public static boolean isLogVisible() {
		return EclipseUtility.getBooleanPreference(LOG_VISIBLE);
	}

	public static void setLogVisible(boolean value) {
		EclipseUtility.setBooleanPreference(LOG_VISIBLE, value);
	}

	/*
	 * Values: 0 = Jetty console log; 1 = Jetty request log.
	 */
	public static final String LOG_SHOWING = PREFIX + "log-showning";

	/**
	 * The log that is showing.
	 * 
	 * @return 0 for Jetty console log, 1 for Jetty request log.
	 */
	public static int getLogShowing() {
		return EclipseUtility.getIntPreference(LOG_SHOWING);
	}

	public static void setLogShowing(int value) {
		EclipseUtility.setIntPreference(LOG_SHOWING, value);
	}

	public static final String PORT = PREFIX + "port";

	public static int getPort() {
		return EclipseUtility.getIntPreference(PORT);
	}

	public static void setPort(int value) {
		EclipseUtility.setIntPreference(PORT, value);
	}

	public static final String SERVER_MEMORY_MB = PREFIX + "server-memory-mb";

	public static int getServerMemoryMB() {
		return EclipseUtility.getIntPreference(SERVER_MEMORY_MB);
	}

	public static void setServerMemoryMB(int mb) {
		EclipseUtility.setIntPreference(SERVER_MEMORY_MB, mb);
	}

	public static final String SERVER_LOGGING_LEVEL = PREFIX
			+ "server-logging-level";

	public static Level getServerLoggingLevel() {
		final String level = EclipseUtility
				.getStringPreference(SERVER_LOGGING_LEVEL);
		return Level.parse(level);
	}

	public static File getSierraLocalTeamServerDirectory() {
		final File dataDir = SierraPreferencesUtility.getSierraDataDirectory();
		final File teamServerDir = new File(dataDir,
				FileUtility.LOCAL_TEAM_SERVER_PATH_FRAGMENT);
		FileUtility.createDirectory(teamServerDir);
		return teamServerDir;
	}

	private LocalTeamServerPreferencesUtility() {
		// utility
	}
}

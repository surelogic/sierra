package com.surelogic.sierra.jdbc.server;

import java.io.File;

import com.surelogic.common.i18n.I18N;

/**
 * Utility methods used to access the local file system by the server. The
 * Sierra server determines where it's files should be located from the property
 * <code>com.surelogic.server</code>.
 * 
 * @author nathan
 * 
 */
public final class ServerFiles {

	private static final String SERVERPROP = "com.surelogic.server";

	private ServerFiles() {

	}

	public static File getSierraLocalTeamServerDirectory() {
		final String loc = System.getProperty(SERVERPROP);
		if (loc == null) {
			throw new IllegalStateException(I18N.err(169));
		}
		return new File(loc);
	}

	public static File getSierraTeamServerCacheDirectory() {
		final File dir = new File(System.getProperty("java.io.tmpdir")
				+ File.separator + "sierra-cache");
		if (!dir.exists()) {
			dir.mkdir();
		}
		return dir;
	}
}

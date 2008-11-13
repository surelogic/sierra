package com.surelogic.sierra.server.lifecycle;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.SchemaUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.schema.SierraSchemaData;

/**
 * This servlet context listener gets the Sierra team server code ready to run.
 * It sets up SLLogger based upon the web.xml context parameters on server
 * startup. It also checks that the database schema is up to date with the code.
 * <p>
 * The parameter <tt>SLLogger</tt> may be set to <tt>serverdir</tt> to cause the
 * logger to log under the ~/Sierra/Server directory. If <tt>SLLogger</tt> is
 * set to <tt>tempdir</tt> the logging will go into the temporary directory. The
 * default (no value) is to log only to the console.
 * <p>
 * The parameter <tt>SLLoggerTag</tt> is set to a string to include in the
 * middle of the log file name. If this parameter is not set a default value of
 * <tt>team-server</tt> is used.
 */
public class BootUpServletContextListener implements ServletContextListener {

	public void contextDestroyed(final ServletContextEvent sce) {
		// nothing to do
	}

	public void contextInitialized(final ServletContextEvent sce) {
		/*
		 * Configure SLLogger based upon the web.xml context parameters.
		 */
		final String loggerOption = sce.getServletContext().getInitParameter(
				"SLLogger");
		final String loggerTag = sce.getServletContext().getInitParameter(
				"SLLoggerTag");
		final String contextName = sce.getServletContext()
				.getServletContextName();
		bootLogging(loggerOption, loggerTag, contextName);
		bootDatabase();
		clearCache();
	}

	private void bootLogging(final String loggerOption, String loggerTag,
			final String contextName) {
		if (loggerTag == null) {
			/*
			 * Set a default
			 */
			loggerTag = "team-server";
		}
		String toString = "";
		if (loggerOption != null) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat(
					"-yyyy_MM_dd");
			final File path;
			final String tail = "log-" + loggerTag
					+ dateFormat.format(new Date()) + ".txt";
			if ("serverdir".equals(loggerOption)) {
				path = FileUtility.getSierraLocalTeamServerDirectory();
			} else {
				path = new File(System.getProperty("java.io.tmpdir"));
			}
			final File logFile = new File(path, tail);
			try {
				final FileHandler fh = new FileHandler(logFile
						.getAbsolutePath(), true);
				SLLogger.addHandler(fh);
			} catch (final Exception e) {
				SLLogger.getLogger().log(Level.SEVERE,
						I18N.err(29, logFile.getAbsolutePath()), e);
			}
			toString = "to '" + logFile.getAbsolutePath() + "' ";
		}
		final Runtime rt = Runtime.getRuntime();
		final long maxMemoryMB = rt.maxMemory() / 1024L / 1024L;
		final long totalMemoryMB = rt.totalMemory() / 1024L / 1024L;
		final long freeMemoryMB = rt.freeMemory() / 1024L / 1024L;
		SLLogger.getLogger().log(
				Level.INFO,
				contextName + " logging " + toString
						+ "initialized : Java runtime: maxMemory="
						+ maxMemoryMB + " MB; totalMemory=" + totalMemoryMB
						+ " MB; freeMemory=" + freeMemoryMB
						+ " MB; availableProcessors="
						+ rt.availableProcessors());
	}

	private void bootDatabase() {
		/*
		 * Bootstrap or update up the database as necessary.
		 */
		ConnectionFactory.getInstance().withTransaction(
				new ServerTransaction<Void>() {
					public Void perform(final Connection conn,
							final Server server) throws Exception {
						SchemaUtility.checkAndUpdate(conn,
								new SierraSchemaData(), true);
						return null;
					}
				});
		SLLogger.getLogger().log(
				Level.INFO,
				"Derby booted with derby.storage.pageSize="
						+ System.getProperty("derby.storage.pageSize")
						+ " and derby.storage.pageCacheSize="
						+ System.getProperty("derby.storage.pageCacheSize")
						+ ".");
	}

	private void clearCache() {
		final File cacheDir = FileUtility.getSierraTeamServerCacheDirectory();
		FileUtility.recursiveDelete(cacheDir);
		SLLogger.getLogger().log(Level.INFO,
				"Cache cleared from " + cacheDir.getAbsolutePath());
	}
}

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
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.schema.SierraSchemaUtility;

/**
 * This servlet context listener gets the Sierra team server code ready to run.
 * It sets up SLLogger based upon the web.xml context parameters on server
 * startup. It also checks that the database schema is up to date with the code.
 * <p>
 * The parameter <tt>SLLogger</tt> may be set to <tt>Use Sierra Directory</tt>
 * to cause the logger to log under the ~/Sierra/Server directory. If
 * <tt>SLLogger</tt> is set to <tt>No File Output</tt> only console logging
 * will be done. The default (no value) is to log into the temporary directory.
 */
public class LogServletContextListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to do
	}

	public void contextInitialized(ServletContextEvent sce) {
		/*
		 * Configure SLLogger based upon the web.xml context parameters.
		 */
		final String loggerOption = sce.getServletContext().getInitParameter(
				"SLLogger");
		final String contextName = sce.getServletContext()
				.getServletContextName();
		bootLogging(loggerOption, contextName);
		bootDatabase();
	}

	private void bootLogging(String loggerOption, String contextName) {
		if ("No File Output".equals(loggerOption))
			return;
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");

		final String logFileName;
		final String tail = File.separator + "log-team-server-"
				+ dateFormat.format(new Date()) + ".txt";
		if ("Use Sierra Directory".equals(loggerOption)) {
			final String serverDirectory = FileUtility.getSierraDataDirectory()
					+ File.separator + "server";
			FileUtility.createDirectory(serverDirectory);
			logFileName = serverDirectory + tail;
		} else {
			logFileName = System.getProperty("java.io.tmpdir") + tail;
		}
		try {
			final FileHandler fh = new FileHandler(logFileName, true);
			SLLogger.addHandler(fh);
		} catch (Exception e) {
			SLLogger.getLogger()
					.log(Level.SEVERE, I18N.err(29, logFileName), e);
		}
		final Runtime rt = Runtime.getRuntime();
		final long maxMemoryMB = rt.maxMemory() / 1024L / 1024L;
		final long totalMemoryMB = rt.totalMemory() / 1024L / 1024L;
		final long freeMemoryMB = rt.freeMemory() / 1024L / 1024L;
		SLLogger.getLogger().info(
				contextName + " logging to '" + logFileName
						+ "' initialized : Java runtime: maxMemory="
						+ maxMemoryMB + " MB; totalMemory=" + totalMemoryMB
						+ " MB; freeMemory=" + freeMemoryMB
						+ " MB; availableProcessors="
						+ rt.availableProcessors());
	}

	private void bootDatabase() {
		/*
		 * Bootstrap or update up the database as necessary.
		 */
		ConnectionFactory.withTransaction(new ServerTransaction<Void>() {
			public Void perform(Connection conn, Server server)
					throws Exception {
				SierraSchemaUtility.checkAndUpdate(conn, true);
				return null;
			}
		});
		SLLogger.log(Level.INFO, "Derby booted with derby.storage.pageSize="
				+ System.getProperty("derby.storage.pageSize")
				+ " and derby.storage.pageCacheSize="
				+ System.getProperty("derby.storage.pageCacheSize") + ".");
	}
}

package com.surelogic.sierra.server.lifecycle;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * This servlet context listener configures SLLogger based upon the web.xml
 * context parameters on server startup.
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
		if ("No File Output".equals(loggerOption))
			return;
		final SimpleDateFormat dateFormat = new SimpleDateFormat("-yyyy_MM_dd");
		String servletContextName = sce.getServletContext()
				.getServletContextName();
		if (servletContextName.length() < 2) {
			servletContextName = "root";
		}
		if (servletContextName.startsWith("/")) {
			servletContextName = servletContextName.substring(1);
		}

		final String logFileName;
		final String tail = File.separator + "log-" + servletContextName
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
	}
}

package com.surelogic.sierra.server.lifecycle;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.logging.SLLogger;

/**
 * This servlet context listener configures SLLogger based upon the web.xml
 * context parameters on server startup.
 * <p>
 * The parameter <tt>SLLogger</tt> may be set to <tt>Use Sierra Directory</tt>
 * to cause the logger to log under the ~/Sierra/Server directory or
 * <tt>No File Output</tt> to cause only console logging.
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
		if ("Use Sierra Directory".equals(loggerOption)) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat(
					"-yyyy.MM.dd-'at'-HH.mm.ss.SSS");
			String logFileName = System.getProperty("user.home")
					+ File.separator + "Sierra" + File.separator + "Server"
					+ File.separator
					+ sce.getServletContext().getServletContextName()
					+ dateFormat.format(new Date()) + ".txt";
			System.setProperty(SLLogger.LOG_FILE_NAME_PROPERTY, logFileName);
		} else if ("No File Output".equals(loggerOption)) {
			System.setProperty(SLLogger.LOG_FILE_NAME_PROPERTY,
					SLLogger.NO_FILE_OUTPUT);
		}
	}
}

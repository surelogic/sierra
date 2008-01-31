package com.surelogic.sierra.server.lifecycle;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"-yyyy.MM.dd-'at'-HH.mm.ss.SSS");
		final String logFileName;
		if ("Use Sierra Directory".equals(loggerOption)) {
			logFileName = System.getProperty("user.home") + File.separator
					+ "Sierra" + File.separator + "Server" + File.separator
					+ sce.getServletContext().getServletContextName()
					+ dateFormat.format(new Date()) + ".txt";
		} else {
			logFileName = System.getProperty("java.io.tmpdir") + File.separator
					+ sce.getServletContext().getServletContextName()
					+ dateFormat.format(new Date()) + ".txt";
		}
		try {
			final FileHandler fh = new FileHandler(logFileName);
			SLLogger.addHandler(fh);
		} catch (Exception e) {
			SLLogger.getLogger()
					.log(Level.SEVERE, I18N.err(29, logFileName), e);
		}
	}
}

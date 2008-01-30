package com.surelogic.sierra.lifecycle;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerConnection;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.schema.SierraSchemaUtility;

/**
 * This context listener bootstraps the database on server startup.
 */
public class SierraContextListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent sce) {
		// Do nothing
	}

	public void contextInitialized(ServletContextEvent sce) {
		/*
		 * TODO: this first part should probably be factored into a superclass
		 * that can be used by both the WARs.
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
		/*
		 * Boot up the database if necessary.
		 */
		ServerConnection.withTransaction(new UserTransaction<Void>() {

			public String getUserName() {
				return "admin";
			}

			public Void perform(Connection conn, Server server)
					throws Exception {
				SierraSchemaUtility.checkAndUpdate(conn, true);
				return null;
			}
		});

	}
}

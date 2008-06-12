package com.surelogic.sierra.server.lifecycle;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.SierraServerLocation;

public class BugLinkServletContextListener implements ServletContextListener {

	public static final int DELAY = 60;
	public static final TimeUnit UNIT = TimeUnit.SECONDS;

	public void contextDestroyed(ServletContextEvent event) {

	}

	public void contextInitialized(ServletContextEvent event) {
		final String server = event.getServletContext().getInitParameter(
				"parent-server");
		final String user = event.getServletContext().getInitParameter(
				"parent-user");
		final String pass = event.getServletContext().getInitParameter(
				"parent-pass");
		try {
			if ((server != null) && !"".equals(server)) {
				ConnectionFactory.lookupTimerService().scheduleWithFixedDelay(
						new Runnable() {
							public void run() {
								try {
									SLLogger.getLogger().info(
											"Updating scan filters and categories from "
													+ server + " at "
													+ new Date());
									final SierraServerLocation location = new SierraServerLocation(
											server, user, pass);
									ConnectionFactory
											.withTransaction(SettingQueries
													.retrieveCategories(
															location,
															ConnectionFactory
																	.withReadOnly(SettingQueries
																			.categoryRequest())));
									ConnectionFactory
											.withTransaction(SettingQueries
													.retrieveScanFilters(
															location,
															ConnectionFactory
																	.withReadOnly(SettingQueries
																			.scanFilterRequest())));
								} catch (final Error e) {
									SLLogger.getLogger().log(Level.SEVERE,
											e.getMessage(), e);
									throw e;
								} catch (final Exception e) {
									SLLogger.getLogger().log(Level.WARNING,
											e.getMessage(), e);
								}
							}
						}, DELAY, DELAY, UNIT);
				SLLogger.getLogger().info(
						"Buglink update scheduled for every " + DELAY + " "
								+ UNIT + " at " + server + ".");
			} else {
				SLLogger.getLogger().info("No parent server configured.");
			}
		} catch (final Exception e) {
			SLLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
	}
}

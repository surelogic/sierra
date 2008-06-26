package com.surelogic.sierra.server.lifecycle;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.settings.ServerLocations;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.SierraServerLocation;

public class BugLinkServletContextListener implements ServletContextListener {

	public static final int DELAY = 300;
	public static final TimeUnit UNIT = TimeUnit.SECONDS;

	public void contextDestroyed(ServletContextEvent event) {

	}

	public void contextInitialized(ServletContextEvent event) {
		try {
			ConnectionFactory.lookupTimerService().scheduleWithFixedDelay(
					new Runnable() {
						public void run() {
							try {
								final Set<SierraServerLocation> locations = ConnectionFactory
										.withReadOnly(
												ServerLocations.fetchQuery(null))
										.keySet();
								SLLogger.getLogger().info(
										"Updating scan filters and categories from "
												+ locations + " at "
												+ new Date());
								for (final SierraServerLocation location : locations) {
									if (location.getHost() != null) {
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
									}
								}
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
					"Buglink update scheduled for every " + DELAY + " " + UNIT
							+ ".");

		} catch (final Exception e) {
			SLLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
	}
}

package com.surelogic.sierra.server.lifecycle;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.settings.ServerLocations;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.ServerInfoReply;
import com.surelogic.sierra.tool.message.ServerInfoRequest;
import com.surelogic.sierra.tool.message.ServerInfoServiceClient;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public class BugLinkServletContextListener implements ServletContextListener {

	public static final int DELAY = 300;
	public static final TimeUnit UNIT = TimeUnit.SECONDS;

	public void contextDestroyed(final ServletContextEvent event) {
		// Do nothing
	}

	public void contextInitialized(final ServletContextEvent event) {
		try {
			ConnectionFactory.getInstance().lookupTimerService()
					.scheduleWithFixedDelay(new Runnable() {
						public void run() {
							try {
								final Set<ServerLocation> locations = ConnectionFactory
										.getInstance().withReadOnly(
												ServerLocations
														.fetchQuery(null))
										.keySet();
								SLLogger.getLogger().info(
										"Updating scan filters and categories from "
												+ locations + " at "
												+ new Date());
								final Map<ServerLocation, ServerInfoReply> validServers = new HashMap<ServerLocation, ServerInfoReply>();
								for (final ServerLocation location : locations) {
									try {
										final ServerInfoReply reply = ServerInfoServiceClient
												.create(location)
												.getServerInfo(
														new ServerInfoRequest());
										validServers.put(location, reply);
									} catch (final SierraServiceClientException e) {
										SLLogger.getLogger().log(Level.INFO,
												e.getMessage(), e);
									}
								}
								ConnectionFactory
										.getInstance()
										.withTransaction(
												ServerLocations
														.updateServerLocationInfo(validServers));
								for (final ServerLocation location : validServers
										.keySet()) {
									ConnectionFactory
											.getInstance()
											.withTransaction(
													SettingQueries
															.retrieveCategories(
																	location,
																	ConnectionFactory
																			.getInstance()
																			.withReadOnly(
																					SettingQueries
																							.categoryRequest())));
									ConnectionFactory
											.getInstance()
											.withTransaction(
													SettingQueries
															.retrieveScanFilters(
																	location,
																	ConnectionFactory
																			.getInstance()
																			.withReadOnly(
																					SettingQueries
																							.scanFilterRequest())));
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

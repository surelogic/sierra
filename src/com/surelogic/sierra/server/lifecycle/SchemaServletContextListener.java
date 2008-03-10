package com.surelogic.sierra.server.lifecycle;

import java.sql.Connection;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.schema.SierraSchemaUtility;

/**
 * This servlet context listener bootstraps or updates the database on server
 * startup.
 */
public class SchemaServletContextListener extends LogServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		/*
		 * Logging setup (courtesy of our parent class)
		 */
		super.contextInitialized(sce);
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
		SLLogger.getLogger().log(
				Level.INFO,
				"Derby initialized with derby.storage.pageSize="
						+ System.getProperty("derby.storage.pageSize")
						+ " and derby.storage.pageCacheSize="
						+ System.getProperty("derby.storage.pageCacheSize")
						+ ".");
	}
}

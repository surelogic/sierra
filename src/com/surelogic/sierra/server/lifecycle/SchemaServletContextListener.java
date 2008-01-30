package com.surelogic.sierra.server.lifecycle;

import java.sql.Connection;

import javax.servlet.ServletContextEvent;

import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerConnection;
import com.surelogic.sierra.jdbc.server.UserTransaction;
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

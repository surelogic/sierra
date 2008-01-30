package com.surelogic.sierra.lifecycle;

import java.sql.Connection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerConnection;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.schema.SierraSchemaUtility;
/**
 * This context listener bootstraps the database on server startup.
 * @author nathan
 *
 */
public class SierraContextListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent arg0) {
		// Do nothing
	}

	public void contextInitialized(ServletContextEvent arg0) {
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

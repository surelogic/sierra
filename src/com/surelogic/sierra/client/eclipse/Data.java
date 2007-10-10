package com.surelogic.sierra.client.eclipse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.core.runtime.IPath;

import com.surelogic.common.eclipse.Derby;
import com.surelogic.sierra.schema.SierraSchemaUtility;

public final class Data {

	private Data() {
		// no instances
	}

	private static final String SCHEMA_NAME = "SIERRA";
	private static final String JDBC_PRE = "jdbc:derby:";
	private static final String JDBC_POST = System
			.getProperty("file.separator")
			+ "db;user=" + SCHEMA_NAME;

	public static void bootAndCheckSchema() throws Exception {

		Derby.bootEmbedded();

		final String connectionURL = getConnectionURL() + ";create=true";
		final Connection c = DriverManager.getConnection(connectionURL);
		c.setAutoCommit(false);
		try {
			SierraSchemaUtility.checkAndUpdate(c, false);
			c.commit();
		} finally {
			c.close();
		}
	}

	public static Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(getConnectionURL());
		return conn;
	}

	private static String getConnectionURL() {
		IPath pluginState = Activator.getDefault().getStateLocation();
		return JDBC_PRE + pluginState.toOSString() + JDBC_POST;
	}
}

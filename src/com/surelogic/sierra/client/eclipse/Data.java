package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IPath;

import com.surelogic.common.FileUtility;
import com.surelogic.common.eclipse.Derby;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.jdbc.LazyPreparedStatementConnection;
import com.surelogic.sierra.schema.SierraSchemaUtility;

public final class Data {

	private Data() {
		// no instances
	}

	private static final String SCHEMA_NAME = "SIERRA";
	private static final String DATABASE_DIR = "db";
	private static final String JDBC_PRE = "jdbc:derby:";
	private static final String JDBC_POST = ";user=" + SCHEMA_NAME;

	public static void bootAndCheckSchema() throws Exception {

		if (PreferenceConstants.deleteDatabaseOnStartup()) {
			/*
			 * Delete the database
			 */
			try {
				final File dbDir = new File(getDatabaseLocation());
				if (dbDir.exists()) {
					if (FileUtility.deleteDirectoryAndContents(dbDir)) {
						SLLogger.getLogger().info(
								"Database deleted at startup : "
										+ getDatabaseLocation());
					} else {
						SLLogger.getLogger().log(
								Level.SEVERE,
								"Unable to delete database at startup : "
										+ getDatabaseLocation());
					}
				}
			} finally {
				PreferenceConstants.setDeleteDatabaseOnStartup(false);
			}
		}

		Derby.bootEmbedded();

		final String connectionURL = getConnectionURL() + ";create=true";
		final Connection c = DriverManager.getConnection(connectionURL);
		Exception e = null;
		try {
			c.setAutoCommit(false);
			SierraSchemaUtility.checkAndUpdate(c, false);
			c.commit();
		} catch (Exception exc) {
			e = exc;
		} finally {
			try {
				c.close();
			} catch (Exception exc) {
				if (e == null) {
					e = exc;
				}
			}
		}
		if (e != null) {
			throw e;
		}
	}

	public static Connection readOnlyConnection() throws SQLException {
		Connection conn = getConnection();
		conn.setReadOnly(true);
		return conn;
	}

	public static Connection transactionConnection() throws SQLException {
		Connection conn = getConnection();
		conn.setAutoCommit(false);
		return conn;
	}

	public static Connection getConnection() throws SQLException {
		Connection conn = LazyPreparedStatementConnection.wrap(DriverManager
				.getConnection(getConnectionURL()));
		return conn;
	}

	private static String getConnectionURL() {
		return JDBC_PRE + getDatabaseLocation() + JDBC_POST;
	}

	private static String getDatabaseLocation() {
		final IPath pluginState = Activator.getDefault().getStateLocation();
		return pluginState.toOSString() + System.getProperty("file.separator")
				+ DATABASE_DIR;
	}
}

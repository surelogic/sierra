package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IPath;

import com.surelogic.common.FileUtility;
import com.surelogic.common.derby.Derby;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.jdbc.ConnectionQuery;
import com.surelogic.sierra.jdbc.DBQuery;
import com.surelogic.sierra.jdbc.DBTransaction;
import com.surelogic.sierra.jdbc.LazyPreparedStatementConnection;
import com.surelogic.sierra.jdbc.server.TransactionException;
import com.surelogic.sierra.schema.SierraSchemaUtility;

public final class Data {

	private Data() {
		// no instances
	}

	private static final String SCHEMA_NAME = "SIERRA";
	private static final String DATABASE_DIR = "db";
	private static final String JDBC_PRE = "jdbc:derby:";
	private static final String JDBC_POST = ";user=" + SCHEMA_NAME;

	private static final Logger log = SLLogger.getLoggerFor(Data.class);

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
		} catch (final Exception exc) {
			e = exc;
		} finally {
			try {
				c.close();
			} catch (final Exception exc) {
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
		final Connection conn = getConnection();
		conn.setReadOnly(true);
		return conn;
	}

	public static Connection transactionConnection() throws SQLException {
		final Connection conn = getConnection();
		conn.setAutoCommit(false);
		return conn;
	}

	public static Connection getConnection() throws SQLException {
		final Connection conn = LazyPreparedStatementConnection
				.wrap(DriverManager.getConnection(getConnectionURL()));
		return conn;
	}

	/**
	 * Perform a query in read-only mode
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public static <T> T withReadOnly(DBQuery<T> action) {
		try {
			return with(readOnlyConnection(), action);
		} catch (final SQLException e) {
			throw new TransactionException("Could not establish connection.", e);
		}
	}

	/**
	 * Perform a query in read-only mode
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public static <T> T withReadOnly(DBTransaction<T> action) {
		try {
			return with(readOnlyConnection(), action);
		} catch (final SQLException e) {
			throw new TransactionException("Could not establish connection.", e);
		}
	}

	/**
	 * Perform a query transaction
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public static <T> T withTransaction(DBQuery<T> action) {
		try {
			return with(transactionConnection(), action);
		} catch (final SQLException e) {
			throw new TransactionException("Could not establish connection.", e);
		}
	}

	/**
	 * Perform a query transaction.
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public static <T> T withTransaction(DBTransaction<T> action) {
		try {
			return with(transactionConnection(), action);
		} catch (final SQLException e) {
			throw new TransactionException("Could not establish connection.", e);
		}
	}

	private static <T> T with(Connection conn, DBTransaction<T> t) {
		Exception exc = null;
		try {
			return t.perform(conn);
		} catch (final Exception exc0) {
			exc = exc0;
		} finally {
			try {
				conn.close();
			} catch (final SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		throw new TransactionException(exc);
	}

	private static <T> T with(Connection conn, DBQuery<T> t) {
		RuntimeException exc = null;
		try {
			return t.perform(new ConnectionQuery(conn));
		} catch (final RuntimeException exc0) {
			exc = exc0;
		} finally {
			try {
				conn.close();
			} catch (final SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		throw exc;
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

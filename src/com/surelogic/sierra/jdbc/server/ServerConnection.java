package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.LazyPreparedStatementConnection;

/**
 * Represents a connection to the Sierra server.
 * 
 * @author nathan
 * 
 */
public class ServerConnection {

	private static final Logger log = SLLogger
			.getLoggerFor(ServerConnection.class);

	private final Connection conn;
	private final boolean readOnly;

	private ServerConnection(boolean readOnly) throws SQLException {
		this.conn = LazyPreparedStatementConnection.wrap(lookup());
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		conn.setReadOnly(readOnly);
		if (!readOnly) {
			conn.setAutoCommit(false);
		}
		this.readOnly = readOnly;
	}

	public Connection getConnection() {
		return conn;
	}

	public String getUid(Connection conn) throws SQLException {
		ResultSet set = conn.createStatement().executeQuery(
				"SELECT UUID FROM SERVER");
		try {
			set.next();
			return set.getString(1);
		} finally {
			set.close();
		}

	}

	/*
	 * It's not clear that we want to do this. We probably want a cleaner way to
	 * finish a transaction.
	 */
	// TODO
	public void finished() throws SQLException {
		if (!readOnly) {
			conn.commit();
		}
		conn.close();
	}

	/**
	 * Increments and returns the server revision number.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public long nextRevision() throws SQLException {
		if (readOnly) {
			throw new IllegalStateException("This connection is read-only");
		}
		PreparedStatement st;
		if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
			st = conn.prepareStatement(
					"INSERT INTO REVISION (DATE_TIME) VALUES (?)",
					new String[] { "REVISION" });
		} else {
			st = conn.prepareStatement(
					"INSERT INTO REVISION (DATE_TIME) VALUES (?)",
					Statement.RETURN_GENERATED_KEYS);
		}
		st.setTimestamp(1, new Timestamp(new Date().getTime()));
		st.execute();
		ResultSet set = st.getGeneratedKeys();
		try {
			set.next();
			return set.getLong(1);
		} finally {
			set.close();
		}

	}

	public String getEmail() throws SQLException {
		try {
			final Connection conn = ServerConnection.readOnly().getConnection();
			try {
				final Statement st = conn.createStatement();
				try {
					final ResultSet set = st
							.executeQuery("SELECT EMAIL FROM NOTIFICATION");
					if (set.next()) {
						return set.getString(1);
					} else {
						return null;
					}
				} catch (SQLException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} finally {
					st.close();
				}
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	public String getUid() throws SQLException {
		ResultSet set = conn.createStatement().executeQuery(
				"SELECT UUID FROM SERVER");
		try {
			set.next();
			return set.getString(1);
		} finally {
			set.close();
		}

	}

	public static ServerConnection transaction() throws SQLException {
		return new ServerConnection(false);
	}

	public static ServerConnection readOnly() throws SQLException {
		return new ServerConnection(true);
	}

	private static Connection lookup() throws SQLException {

		try {
			InitialContext context = new InitialContext();
			try {
				return (Connection) ((DataSource) new InitialContext()
						.lookup("jdbc/Sierra")).getConnection();
			} finally {
				context.close();
			}
		} catch (NamingException e) {
			throw new IllegalStateException(e);
		}
	}
}

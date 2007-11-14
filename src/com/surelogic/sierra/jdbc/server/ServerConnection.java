package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.surelogic.sierra.jdbc.LazyPreparedStatementConnection;

public class ServerConnection {

	private final Connection conn;
	private final boolean readOnly;

	private ServerConnection(boolean readOnly) throws SQLException {
		this.conn = LazyPreparedStatementConnection.wrap(lookup());
		conn.setReadOnly(readOnly);
		if(!readOnly) {
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

	@Deprecated
	/*
	 * It's not clear that we want to do this.  We probably want a cleaner way to finish a transaction.
	 */
	public void finished() throws SQLException {
		if (!readOnly) {
			conn.commit();
		}
		conn.close();
	}

	public static ServerConnection transaction() throws SQLException {
		return new ServerConnection(false);
	}

	public static ServerConnection readOnly() throws SQLException {
		final Connection conn = lookup();
		conn.setReadOnly(true);
		return new ServerConnection(true);
	}

	private static Connection lookup() throws SQLException {
		try {
			return (Connection) ((DataSource) new InitialContext()
					.lookup("jdbc/Sierra")).getConnection();
		} catch (NamingException e) {
			throw new IllegalStateException(e);
		}
	}
}

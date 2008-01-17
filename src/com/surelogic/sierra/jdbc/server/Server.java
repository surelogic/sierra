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

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;

/**
 * Server represents the global database server state, including server uid and
 * server revision.
 * 
 * @author nathan
 * 
 */
public class Server {

	private static final Logger log = SLLogger.getLoggerFor(Server.class);

	private final Connection conn;
	private final boolean readOnly;

	Server(Connection conn, boolean readOnly) {
		this.conn = conn;
		this.readOnly = readOnly;
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

	/**
	 * Returns the administrative email of this server.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public String getEmail() throws SQLException {
		try {
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

	/**
	 * Get the server uid.
	 * 
	 * @return
	 * @throws SQLException
	 */
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
	

	/**
	 * Get the server uid.
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String getUid(Connection conn) throws SQLException {
		ResultSet set = conn.createStatement().executeQuery(
				"SELECT UUID FROM SERVER");
		try {
			set.next();
			return set.getString(1);
		} finally {
			set.close();
		}

	}

}

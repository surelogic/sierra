package com.surelogic.sierra.jdbc.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.jdbc.FutureDatabaseException;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.schema.SierraSchemaUtility;

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
	 * Returns the current schema version of this server. Possible values
	 * consist of <code>None</code>, <code>Error</code>, and the natural
	 * numbers including 0.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public String getSchemaVersion() throws SQLException {
		final Statement st = conn.createStatement();
		try {
			final ResultSet set = st.executeQuery("SELECT N FROM VERSION");
			try {
				if (set.next()) {
					return Integer.toString(set.getInt(1));
				} else {
					return "None";
				}
			} finally {
				set.close();
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			st.close();
		}
		return "Error";
	}

	/**
	 * Returns the available schema version of this server. Possible values
	 * consist of the set of natural numbers including 0.xs
	 * 
	 * @return
	 */
	public String getAvailableSchemaVersion() {
		return Integer.toString(SierraSchemaUtility.schemaVersion);
	}

	/**
	 * Updates the schema to the highest available version.
	 * 
	 * @throws SQLException
	 * @throws FutureDatabaseException
	 */
	public void updateSchema() throws SQLException, FutureDatabaseException {
		try {
			SierraSchemaUtility.checkAndUpdate(conn, true);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
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
		try {
			st.setTimestamp(1, new Timestamp(new Date().getTime()));
			st.execute();
			ResultSet set = st.getGeneratedKeys();
			try {
				set.next();
				return set.getLong(1);
			} finally {
				set.close();
			}
		} finally {
			st.close();
		}
	}

	/**
	 * @throws SQLException
	 * 
	 */
	public void notifyAdmin(String subject, String message) throws SQLException {
//TODO
//		String email = getEmail();
//		if (email != null) {
//			Properties props = new Properties();
//			props.put("mail.smtp.host", "mail.surelogic.com");
//			props.put("mail.from", email);
//			Session session = Session.getInstance(props, null);
//			try {
//				MimeMessage msg = new MimeMessage(session);
//				msg.setFrom();
//				msg.setRecipients(Message.RecipientType.TO, email);
//				msg.setSubject(subject);
//				msg.setSentDate(new Date());
//				msg.setText(message);
//				Transport.send(msg);
//			} catch (MessagingException mex) {
//				log.log(Level.SEVERE, "Mail notification of exception failed.",
//						mex);
//			}
//		}
	}

	/**
	 * Returns the administrative email of this server.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public String getEmail() throws SQLException {
		try {
			final Statement st = conn.createStatement();
			try {
				final ResultSet set = st
						.executeQuery("SELECT EMAIL FROM NOTIFICATION");
				try {
					if (set.next()) {
						return set.getString(1);
					} else {
						return null;
					}
				} finally {
					set.close();
				}
			} finally {
				st.close();
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Change the notification email address.
	 * 
	 * @param address
	 * @throws SQLException
	 */
	public void setEmail(String address) throws SQLException {
		final Statement st = conn.createStatement();
		try {
			st.execute("DELETE FROM NOTIFICATION");
			st.execute("INSERT INTO NOTIFICATION (EMAIL) VALUES ('"
					+ JDBCUtils.escapeString(address) + "')");
		} finally {
			st.close();
		}
	}

	/**
	 * Get the server uid.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public String getUid() throws SQLException {
		Statement s = conn.createStatement();
		try {
			ResultSet set = s.executeQuery("SELECT UUID FROM SERVER");
			try {
				set.next();
				return set.getString(1);
			} finally {
				set.close();
			}
		} finally {
			s.close();
		}
	}

}

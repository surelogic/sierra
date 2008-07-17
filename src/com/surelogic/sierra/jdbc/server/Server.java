package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBType;
import com.surelogic.common.jdbc.FutureDatabaseException;
import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jdbc.SchemaUtility;
import com.surelogic.common.jdbc.StatementException;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.schema.SierraSchemaData;

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

	Server(final Connection conn, final boolean readOnly) {
		this.conn = conn;
		this.readOnly = readOnly;
	}

	/**
	 * Returns the current schema version of this server. Possible values
	 * consist of <code>None</code>, <code>Error</code>, and the natural numbers
	 * including 0.
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
		} catch (final SQLException e) {
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
		return Integer.toString(new SierraSchemaData().getVersion());
	}

	/**
	 * Updates the schema to the highest available version.
	 * 
	 * @throws SQLException
	 * @throws FutureDatabaseException
	 */
	public void updateSchema() throws SQLException, FutureDatabaseException {
		SchemaUtility.checkAndUpdate(conn, new SierraSchemaData(), true);
	}

	/**
	 * Increments and returns the server revision number.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public long nextRevision() {
		if (readOnly) {
			throw new IllegalStateException("This connection is read-only");
		}
		try {
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
				final ResultSet set = st.getGeneratedKeys();
				try {
					set.next();
					return set.getLong(1);
				} finally {
					set.close();
				}
			} finally {
				st.close();
			}
		} catch (final Exception e) {
			throw new TransactionException(e);
		}
	}

	/**
	 * @throws SQLException
	 * 
	 */
	public void notifyAdmin(final String subject, final String message)
			throws SQLException {
		final Notification n = getNotification();
		if (n != null) {
			final String to = n.getToEmail();
			final String from = n.getFromEmail();
			final String host = n.getHost();
			final Integer port = n.getPort();
			final String pass = n.getPass();
			final String user = n.getUser();
			if ((to != null) && (to.length() > 0) && (host != null)
					&& (host.length() > 0)) {
				final Properties props = new Properties();
				props.setProperty("mail.transport.protocol", "smtp");
				props.setProperty("mail.smtp.host", host);
				props.setProperty("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.auth", "true");
				if (port != null) {
					props.put("mail.smtp.port", port);
				}
				Authenticator auth;
				if ((user != null) && (user.length() > 0)) {
					auth = new Authenticator() {

						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(user, pass);
						}

					};
				} else {
					auth = null;
				}
				final Session session = Session.getInstance(props, auth);
				try {
					final MimeMessage msg = new MimeMessage(session);
					msg.setSender(new InternetAddress(((from == null) || (from
							.length() == 0)) ? to : from));
					msg.setRecipient(Message.RecipientType.TO,
							new InternetAddress(to));
					msg.setSubject(subject);
					msg.setSentDate(new Date());
					msg.setContent(message, "text/plain");
					Transport.send(msg);
				} catch (final MessagingException mex) {
					log.log(Level.SEVERE,
							"Mail notification of exception failed.", mex);
				}
			}
		}
	}

	/**
	 * Get the server uid.
	 * 
	 * @return
	 */
	public String getUid() {
		try {
			final Statement s = conn.createStatement();
			try {
				final ResultSet set = s.executeQuery("SELECT UUID FROM SERVER");
				try {
					set.next();
					return set.getString(1);
				} finally {
					set.close();
				}
			} finally {
				s.close();
			}
		} catch (final SQLException e) {
			throw new StatementException(e);
		}
	}

	public Notification getNotification() throws SQLException {
		final Statement st = conn.createStatement();
		try {
			final ResultSet set = st
					.executeQuery("SELECT SMTP_HOST,SMTP_PORT,SMTP_USER,SMTP_PASS,TO_EMAIL,FROM_EMAIL FROM NOTIFICATION");
			try {
				if (set.next()) {
					int idx = 1;
					return new Notification(set.getString(idx++), JDBCUtils
							.getNullableInteger(idx++, set), set
							.getString(idx++), set.getString(idx++), set
							.getString(idx++), set.getString(idx++));
				} else {
					return null;
				}
			} finally {
				set.close();
			}
		} finally {
			st.close();
		}
	}

	public void setNotification(final Notification notification)
			throws SQLException {
		final Statement st = conn.createStatement();
		try {
			st.execute("DELETE FROM NOTIFICATION");
			st
					.execute("INSERT INTO NOTIFICATION (SMTP_HOST,SMTP_PORT,SMTP_USER,SMTP_PASS,FROM_EMAIL,TO_EMAIL) VALUES "
							+ escapedTuple(new Object[] {
									notification.getHost(),
									notification.getPort(),
									notification.getUser(),
									notification.getPass(),
									notification.getFromEmail(),
									notification.getToEmail() }));
		} finally {
			st.close();
		}
	}

	private static String escapedTuple(final Object[] values) {
		final StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(getLiteral(values[0]));
		for (int i = 1; i < values.length; i++) {
			sb.append(',');
			sb.append(getLiteral(values[i]));
		}
		sb.append(')');
		return sb.toString();
	}

	private static String getLiteral(final Object value) {
		if (value == null) {
			return "NULL";
		} else if (value instanceof String) {
			return "'" + JDBCUtils.escapeString(value.toString()) + "'";
		} else {
			return value.toString();
		}
	}

	/**
	 * Returns the current team server version.
	 * 
	 * @return
	 */
	public static String getSoftwareVersion() {
		return I18N.msg("sierra.teamserver.version");
	}
}

package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
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
import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.DBType;
import com.surelogic.common.jdbc.FutureDatabaseException;
import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.SchemaUtility;
import com.surelogic.common.jdbc.StatementException;
import com.surelogic.common.jdbc.StringResultHandler;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
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
	 * Returns the current team server version.
	 * 
	 * @return
	 */
	public static String getSoftwareVersion() {
		return I18N.msg("sierra.teamserver.version");
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
	 * If notification settings are present, send an email notification with the
	 * given subject and message.
	 * 
	 * @param subject
	 *            subject of the email
	 * @param message
	 *            email body
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
			final String pass = n.getPassword();
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

	/**
	 * Retrieves a single site setting value.
	 * 
	 * @param key
	 *            the site setting's name
	 * @return value of the site setting, or null if it is not set
	 * @throws SQLException
	 * @see {@link #getSiteSettings(String, String...)}
	 */
	public String getSiteSetting(final String key) throws SQLException {
		return getSiteSettings(null, key).get(key);
	}

	/**
	 * Retrieves the name and value of a list of site settings.
	 * 
	 * @param keyPrefix
	 *            a prefix to append to all key names, null is valid empty
	 *            prefix
	 * @param keys
	 *            the list of keys to retrieve
	 * @return a name/value map of settings
	 * @throws SQLException
	 */
	public Map<String, String> getSiteSettings(final String keyPrefix,
			final String... keys) throws SQLException {
		final Statement st = conn.createStatement();
		try {
			final StringBuilder sql = new StringBuilder();
			sql
					.append("SELECT SETTING_NAME, SETTING_VALUE FROM SITE_SETTINGS WHERE SETTING_NAME IN (");
			boolean first = true;
			for (final String key : keys) {
				if (first) {
					first = false;
				} else {
					sql.append(", ");
				}
				String fqKey;
				if (keyPrefix != null) {
					fqKey = keyPrefix + "." + key;
				} else {
					fqKey = key;
				}
				sql.append(getLiteral(fqKey));
			}
			sql.append(")");
			final ResultSet rs = st.executeQuery(sql.toString());
			try {
				final Map<String, String> settings = new HashMap<String, String>();
				while (rs.next()) {
					final String fqKey = rs.getString("SETTING_NAME");
					String key;
					if (keyPrefix != null) {
						key = fqKey.substring(keyPrefix.length() + 1);
					} else {
						key = fqKey;
					}
					settings.put(key, rs.getString("SETTING_VALUE"));
				}
				return settings;
			} finally {
				rs.close();
			}
		} finally {
			st.close();
		}
	}

	/**
	 * Stores a single site setting value.
	 * 
	 * @param key
	 *            the setting's name
	 * @param value
	 *            the setting's value
	 * @throws SQLException
	 * @see {@link #setSiteSettings(Map)}
	 */
	public void setSiteSetting(final String key, final String value)
			throws SQLException {
		final Map<String, String> settings = new HashMap<String, String>();
		settings.put(key, value);
		setSiteSettings(null, settings);
	}

	/**
	 * Stores a list of settings, overwriting existing values.
	 * 
	 * @param keyPrefix
	 *            an option prefix for each key
	 * @param settings
	 *            a map of setting name and value pairs
	 * @throws SQLException
	 */
	public void setSiteSettings(final String keyPrefix,
			final Map<String, String> settings) throws SQLException {
		final Statement st = conn.createStatement();
		try {
			final StringBuilder deleteSql = new StringBuilder();
			deleteSql
					.append("DELETE FROM SITE_SETTINGS WHERE SETTING_NAME IN (");

			final StringBuilder insertSql = new StringBuilder();
			insertSql
					.append("INSERT INTO SITE_SETTINGS (SETTING_NAME, SETTING_VALUE) VALUES ");
			boolean first = true;
			for (final Entry<String, String> setting : settings.entrySet()) {
				String fqKey = setting.getKey();
				if (keyPrefix != null) {
					fqKey = keyPrefix + "." + fqKey;
				}
				if (first) {
					first = false;
				} else {
					deleteSql.append(", ");
					insertSql.append(", ");
				}
				deleteSql.append(getLiteral(fqKey));

				insertSql.append('(').append(getLiteral(fqKey));
				insertSql.append(", ").append(getLiteral(setting.getValue()))
						.append(')');
			}
			deleteSql.append(')');

			st.executeUpdate(deleteSql.toString());
			st.executeUpdate(insertSql.toString());
		} finally {
			st.close();
		}
	}

	/**
	 * Retrieves email notification settings for the server.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Notification getNotification() throws SQLException {
		final Map<String, String> settings = getSiteSettings("notification",
				"ToEmail", "FromEmail", "SmtpHost", "SmtpPort", "SmtpUser",
				"SmtpPass");
		if (settings.size() == 0) {
			return null;
		}

		final Notification ntfn = new Notification();
		ntfn.setToEmail(settings.get("ToEmail"));
		ntfn.setFromEmail(settings.get("FromEmail"));
		ntfn.setHost(settings.get("SmtpHost"));
		try {
			ntfn.setPort(Integer.valueOf(settings.get("SmtpPort")));
		} catch (final NumberFormatException nfe) {
			ntfn.setPort(25);
		}
		ntfn.setUser(settings.get("SmtpUser"));
		ntfn.setPassword(settings.get("SmtpPass"));
		return ntfn;
	}

	/**
	 * Stores email notification settings.
	 * 
	 * @param notification
	 * @throws SQLException
	 */
	public void setNotification(final Notification notification)
			throws SQLException {
		final Map<String, String> settings = new HashMap<String, String>();
		settings.put("ToEmail", notification.getToEmail());
		settings.put("FromEmail", notification.getFromEmail());
		settings.put("SmtpHost", notification.getHost());
		final Integer port = notification.getPort();
		settings.put("SmtpPort", port == null ? null : port.toString());
		settings.put("SmtpUser", notification.getUser());
		settings.put("SmtpPass", notification.getPassword());
		setSiteSettings("notification", settings);
	}

	/**
	 * Get the server name.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public String getName() throws SQLException {
		return getSiteSetting("Name");
	}

	/**
	 * Set the server name. This will also modify the name of the default scan
	 * filter (as long as we own it!).
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public void setName(final String name) throws SQLException {
		if (name == null) {
			throw new IllegalArgumentException("Name may not be null.");
		}
		setSiteSetting("Name", name);
		final Query q = new ConnectionQuery(conn);
		final ScanFilters sf = new ScanFilters(q);
		final ScanFilterDO filter = sf.getDefaultScanFilter();
		if (q.prepared("Definitions.getDefinitionServer",
				new StringResultHandler()).call(filter.getUid()).equals(
				getUid())) {
			filter.setName(String.format("Defaults for %s", name));
			sf.updateScanFilter(filter, nextRevision());
		}
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

}

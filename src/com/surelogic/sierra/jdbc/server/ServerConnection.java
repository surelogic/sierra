package com.surelogic.sierra.jdbc.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.LazyPreparedStatementConnection;

public class ServerConnection {

	private static final Logger log = SLLogger
			.getLoggerFor(ServerConnection.class);

	protected final Connection conn;

	protected final boolean readOnly;
	protected final Server server;

	ServerConnection(Connection conn, boolean readOnly) throws SQLException {
		this.conn = LazyPreparedStatementConnection.wrap(conn);
		this.conn
				.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		this.conn.setReadOnly(readOnly);
		if (!readOnly) {
			this.conn.setAutoCommit(false);
		}
		this.readOnly = readOnly;
		this.server = new Server(conn, readOnly);
	}

	public Connection getConnection() {
		return conn;
	}

	public Server getServer() {
		return server;
	}

	/**
	 * Finished should always be called when the user is done with the server
	 * connection.
	 * 
	 * @throws SQLException
	 */
	public void finished() throws SQLException {
		if (!readOnly) {
			conn.commit();
		}
		conn.close();
	}

	/**
	 * Perform the specified transaction. If an exception occurs while executing
	 * this method, the server notifies the administrator of an error. In
	 * addition, the transaction is rolled back.
	 * 
	 * @throws TransactionException
	 *             when an error occurs while executing the transaction
	 * @param <T>
	 * @param t
	 * @return
	 */
	public <T> T perform(ServerTransaction<T> t) {
		try {
			final T val = t.perform(conn, server);
			if (!readOnly) {
				conn.commit();
			}
			return val;
		} catch (Exception e) {
			if (!readOnly) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					log.log(Level.WARNING, e1.getMessage(), e1);
				}
			}
			exceptionNotify("Server", e.getMessage(), e);
			throw new TransactionException(e);
		}
	}

	/*
	 * Send mail to the listed admin email that an exception has occurred while
	 * processing a transaction.
	 */
	protected void exceptionNotify(String userName, String message, Throwable t) {
		log.log(Level.SEVERE, message, t);
		try {
			String email = server.getEmail();
			if (email != null) {
				Properties props = new Properties();
				props.put("mail.smtp.host", "zimbra.surelogic.com");
				props.put("mail.from", email);
				Session session = Session.getInstance(props, null);
				try {
					MimeMessage msg = new MimeMessage(session);
					msg.setFrom();
					msg.setRecipients(Message.RecipientType.TO, email);
					StringWriter s = new StringWriter();
					t.printStackTrace(new PrintWriter(s));
					msg.setSubject(userName + " reports: " + message);
					msg.setSentDate(new Date());
					msg.setText(s.toString());
					Transport.send(msg);
				} catch (MessagingException mex) {
					log.log(Level.SEVERE,
							"Mail notification of exception failed.", mex);
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}

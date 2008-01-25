package com.surelogic.sierra.jdbc.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.LazyPreparedStatementConnection;

/**
 * Represents a connection to the Sierra server. This connection allows people
 * to execute SQL transactions against the server, both synchronously and
 * asynchronously.
 * 
 * @author nathan
 * 
 */
public class ServerConnection {

	private static final Logger log = SLLogger
			.getLoggerFor(ServerConnection.class);

	private final Connection conn;
	private final boolean readOnly;
	private final Server server;

	private ServerConnection(boolean readOnly) throws SQLException {
		this.conn = LazyPreparedStatementConnection.wrap(lookupConnection());
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		conn.setReadOnly(readOnly);
		if (!readOnly) {
			conn.setAutoCommit(false);
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
	public <T> T perform(UserTransaction<T> t) {
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
			exceptionNotify(t.getUserName(), e.getMessage(), e);
			throw new TransactionException(e);
		}
	}

	/**
	 * Return a connection to the server capable of executing transactions.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static ServerConnection transaction() throws SQLException {
		return new ServerConnection(false);
	}

	/**
	 * Return a connection to the server in read-only mode.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static ServerConnection readOnly() throws SQLException {
		return new ServerConnection(true);
	}

	/**
	 * Queue a transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> Future<T> delayTransaction(final UserTransaction<T> t) {
		return lookupExecutor().submit(new Callable<T>() {

			public T call() throws Exception {
				return ServerConnection.withTransaction(t);
			}});
	}

	/**
	 * Queue a transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> Future<T> delayReadOnly(final UserTransaction<T> t) {
		return lookupExecutor().submit(new Callable<T>() {

			public T call() throws Exception {
				return ServerConnection.withReadOnly(t);
			}});
	}

	/**
	 * Retrieve a connection, and execute the given user transaction.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> T withTransaction(UserTransaction<T> t) {
		try {
			return with(transaction(), t);
		} catch (SQLException e) {
			throw new TransactionException(e);
		}
	}

	/**
	 * Retrieve a connection, and execute the given read-only user transaction.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> T withReadOnly(UserTransaction<T> t) {
		try {
			return with(readOnly(), t);
		} catch (SQLException e) {
			throw new TransactionException(e);
		}
	}

	private static <T> T with(ServerConnection server, UserTransaction<T> t) {
		RuntimeException exc = null;
		try {
			return server.perform(t);
		} catch (RuntimeException exc0) {
			exc = exc0;
		} finally {
			try {
				server.finished();
			} catch (SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		throw exc;
	}

	/*
	 * Send mail to the listed admin email that an exception has occurred while
	 * processing a transaction.
	 */
	private void exceptionNotify(String userName, String message, Throwable t) {
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

	/*
	 * Look up the JDBC data source.
	 */
	private static Connection lookupConnection() throws SQLException {
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

	/*
	 * Look up the transaction handler.
	 */
	private static ExecutorService lookupExecutor() {
		try {
			InitialContext context = new InitialContext();
			try {
				return (ExecutorService) new InitialContext()
						.lookup("SierraTransactionHandler");
			} finally {
				context.close();
			}
		} catch (NamingException e) {
			throw new IllegalStateException(e);
		}
	}

}

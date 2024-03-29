package com.surelogic.sierra.jdbc.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import com.surelogic.common.derby.LazyPreparedStatementConnection;
import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.DBTransaction;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.logging.SLLogger;

public class ServerConnection {

	private static final Logger log = SLLogger
			.getLoggerFor(ServerConnection.class);

	protected final Connection conn;

	protected final boolean readOnly;
	protected final Server server;

	/**
	 * Construct a server connection, but do not explicitly set a transaction
	 * mode.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	ServerConnection(final Connection conn) throws SQLException {
		this.conn = LazyPreparedStatementConnection.wrap(conn);
		this.readOnly = false;
		server = new Server(conn, readOnly);
	}

	/**
	 * Construct a server connection with the given transactional mode
	 * 
	 * @param conn
	 * @param readOnly
	 *            whether the server is read only, or transacted
	 * @throws SQLException
	 */
	public ServerConnection(final Connection conn, final boolean readOnly)
			throws SQLException {
		this.conn = LazyPreparedStatementConnection.wrap(conn);
		this.conn
				.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		this.conn.setReadOnly(readOnly);
		if (!readOnly) {
			this.conn.setAutoCommit(false);
		}
		this.readOnly = readOnly;
		server = new Server(conn, readOnly);
	}

	public ServerConnection(final Connection conn, final boolean readOnly,
			final int isolationLevel) throws SQLException {
		this.conn = LazyPreparedStatementConnection.wrap(conn);
		this.conn.setTransactionIsolation(isolationLevel);
		this.conn.setReadOnly(readOnly);
		if (!readOnly) {
			this.conn.setAutoCommit(false);
		}
		this.readOnly = readOnly;
		server = new Server(conn, readOnly);
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
	 * Perform the specified query transaction. If an exception occurs while
	 * executing this method, the server notifies the administrator of an error.
	 * In addition, the transaction is rolled back.
	 * 
	 * @throws TransactionException
	 *             when an error occurs while executing the transaction
	 * @param <T>
	 * @param t
	 * @return
	 */
	public <T> T perform(final DBQuery<T> t) {
		try {
			final T val = t.perform(new ConnectionQuery(conn));
			if (!readOnly) {
				conn.commit();
			}
			return val;
		} catch (final Exception e) {
			if (!readOnly) {
				try {
					conn.rollback();
				} catch (final SQLException e1) {
					log.log(Level.WARNING, e1.getMessage(), e1);
				}
			}
			exceptionNotify("Server", e.getMessage(), e);
			throw new TransactionException(e);
		}
	}

	/**
	 * Perform the specified query transaction. If an exception occurs while
	 * executing this method, the server notifies the administrator of an error.
	 * In addition, the transaction is rolled back.
	 * 
	 * @throws TransactionException
	 *             when an error occurs while executing the transaction
	 * @param <T>
	 * @param t
	 * @return
	 */
	public <T> T perform(final DBTransaction<T> t) {
		try {
			final T val = t.perform(conn);
			if (!readOnly) {
				conn.commit();
			}
			return val;
		} catch (final Exception e) {
			if (!readOnly) {
				try {
					conn.rollback();
				} catch (final SQLException e1) {
					log.log(Level.WARNING, e1.getMessage(), e1);
				}
			}
			exceptionNotify("Server", e.getMessage(), e);
			throw new TransactionException(e);
		}
	}

	/**
	 * Perform the specified query transaction. If an exception occurs while
	 * executing this method, the server notifies the administrator of an error.
	 * In addition, the transaction is rolled back.
	 * 
	 * @throws TransactionException
	 *             when an error occurs while executing the transaction
	 * @param <T>
	 * @param t
	 * @return
	 */
	public <T> T perform(final ServerQuery<T> t) {
		try {
			final T val = t.perform(new ConnectionQuery(conn), server);
			if (!readOnly) {
				conn.commit();
			}
			return val;
		} catch (final Exception e) {
			if (!readOnly) {
				try {
					conn.rollback();
				} catch (final SQLException e1) {
					log.log(Level.WARNING, e1.getMessage(), e1);
				}
			}
			exceptionNotify("Server", e.getMessage(), e);
			throw new TransactionException(e);
		}
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
	public <T> T perform(final ServerTransaction<T> t) {
		try {
			final T val = t.perform(conn, server);
			if (!readOnly) {
				conn.commit();
			}
			return val;
		} catch (final Exception e) {
			if (!readOnly) {
				try {
					conn.rollback();
				} catch (final SQLException e1) {
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
	protected void exceptionNotify(final String userName, final String message,
			final Throwable t) {
		log.log(Level.SEVERE, message, t);
		try {
			final StringWriter s = new StringWriter();
			final PrintWriter p = new PrintWriter(s);
			t.printStackTrace(p);
			p.flush();
			server.notifyAdmin(userName + " reports: " + message, s.toString());
		} catch (final SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (final MessagingException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}

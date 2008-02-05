package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.user.User;

/**
 * Represents a connection to the Sierra server. This connection allows people
 * to execute SQL transactions against the server, both synchronously and
 * asynchronously.
 * 
 * @author nathan
 * 
 */
public class UserConnection extends ServerConnection {

	private static final Logger log = SLLogger
			.getLoggerFor(UserConnection.class);

	protected final User user;

	UserConnection(Connection conn, User user, boolean readOnly)
			throws SQLException {
		super(conn, readOnly);
		this.user = user;
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
			final T val = t.perform(conn, server, user);
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
			exceptionNotify(user.getName(), e.getMessage(), e);
			throw new TransactionException(e);
		}
	}

}

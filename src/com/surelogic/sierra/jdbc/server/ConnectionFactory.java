package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.DBTransaction;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.user.User;

public final class ConnectionFactory {

	private static final Logger log = SLLogger
			.getLoggerFor(ConnectionFactory.class);

	/**
	 * Return a connection to the server capable of executing transactions.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static ServerConnection transaction() throws SQLException {
		return new ServerConnection(lookupConnection(), false);
	}

	/**
	 * Return a connection to the server in read-only mode.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static ServerConnection readUncommitted() throws SQLException {
		return new ServerConnection(lookupConnection(), true,
				Connection.TRANSACTION_READ_COMMITTED);
	}

	/**
	 * Return a connection to the server in read-only mode.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static ServerConnection readOnly() throws SQLException {
		return new ServerConnection(lookupConnection(), true);
	}

	/**
	 * Return a connection to the server capable of executing transactions.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static UserConnection userTransaction() throws SQLException {
		return new UserConnection(lookupConnection(), lookupUser(), false);
	}

	/**
	 * Return a connection to the server in read-only mode.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static UserConnection userReadOnly() throws SQLException {
		return new UserConnection(lookupConnection(), lookupUser(), true);
	}

	/**
	 * Queue a transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static void scheduleTransactionWithFixedDelay(
			final ServerQuery<?> t, final long initialDelay, final long delay,
			final TimeUnit unit) {
		lookupTimerService().scheduleWithFixedDelay(new Runnable() {
			public void run() {
				try {
					with(new ServerConnection(lookupConnection(), false), t);
				} catch (final SQLException e) {
					throw new IllegalStateException();
				}

			}
		}, initialDelay, delay, unit);
	}

	/**
	 * Queue a transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static void scheduleTransactionWithFixedDelay(
			final ServerTransaction<?> t, final long initialDelay,
			final long delay, final TimeUnit unit) {
		lookupTimerService().scheduleWithFixedDelay(new Runnable() {
			public void run() {
				try {
					with(new ServerConnection(lookupConnection(), false), t);
				} catch (final SQLException e) {
					throw new IllegalStateException();
				}
			}
		}, initialDelay, delay, unit);
	}

	/**
	 * Queue a transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> Future<T> delayTransaction(final ServerQuery<T> t) {
		return lookupExecutor().submit(new Callable<T>() {

			public T call() throws Exception {
				return with(new ServerConnection(lookupConnection(), false), t);
			}
		});
	}

	/**
	 * Queue a transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> Future<T> delayTransaction(final ServerTransaction<T> t) {
		return lookupExecutor().submit(new Callable<T>() {

			public T call() throws Exception {
				return with(new ServerConnection(lookupConnection(), false), t);
			}
		});
	}

	/**
	 * Queue a read-only transactionto occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> Future<T> delayReadOnly(final ServerQuery<T> t) {
		return lookupExecutor().submit(new Callable<T>() {

			public T call() throws Exception {
				return with(new ServerConnection(lookupConnection(), true), t);
			}
		});
	}

	/**
	 * Queue a read-only transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> Future<T> delayReadOnly(final ServerTransaction<T> t) {
		return lookupExecutor().submit(new Callable<T>() {

			public T call() throws Exception {
				return with(new ServerConnection(lookupConnection(), true), t);
			}
		});
	}

	/**
	 * Queue a transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> Future<T> delayUserTransaction(final UserQuery<T> t) {
		final User user = lookupUser();
		return lookupExecutor().submit(new Callable<T>() {

			public T call() throws Exception {
				return withUser(new UserConnection(lookupConnection(), user,
						false), t);
			}
		});
	}

	/**
	 * Queue a transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> Future<T> delayUserTransaction(final UserTransaction<T> t) {
		final User user = lookupUser();
		return lookupExecutor().submit(new Callable<T>() {

			public T call() throws Exception {
				return withUser(new UserConnection(lookupConnection(), user,
						false), t);
			}
		});
	}

	/**
	 * Queue a transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> Future<T> delayUserReadOnly(final UserQuery<T> t) {
		final User user = lookupUser();
		return lookupExecutor().submit(new Callable<T>() {

			public T call() throws Exception {
				return withUser(new UserConnection(lookupConnection(), user,
						true), t);
			}
		});
	}

	/**
	 * Queue a transaction to occur at a later time.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> Future<T> delayUserReadOnly(final UserTransaction<T> t) {
		final User user = lookupUser();
		return lookupExecutor().submit(new Callable<T>() {

			public T call() throws Exception {
				return withUser(new UserConnection(lookupConnection(), user,
						true), t);
			}
		});
	}

	/**
	 * Retrieve a connection, and execute the given user transaction.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> T withTransaction(final DBQuery<T> t) {
		try {
			return with(transaction(), t);
		} catch (final SQLException e) {
			throw new TransactionException(e);
		}
	}

	/**
	 * Retrieve a connection, and execute the given user transaction.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> T withTransaction(final DBTransaction<T> t) {
		try {
			return with(transaction(), t);
		} catch (final SQLException e) {
			throw new TransactionException(e);
		}
	}

	/**
	 * Retrieve a connection, and execute the given user transaction.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> T withTransaction(final ServerQuery<T> t) {
		try {
			return with(transaction(), t);
		} catch (final SQLException e) {
			throw new TransactionException(e);
		}
	}

	/**
	 * Retrieve a connection, and execute the given user transaction.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> T withTransaction(final ServerTransaction<T> t) {
		try {
			return with(transaction(), t);
		} catch (final SQLException e) {
			throw new TransactionException(e);
		}
	}

	/**
	 * Retrieve a connection, and execute the given read-only user transaction
	 * w/ a transaction isolation level of READ_UNCOMMITTED.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> T withReadUncommitted(final ServerQuery<T> t) {
		try {
			return with(readUncommitted(), t);
		} catch (final SQLException e) {
			throw new TransactionException(e);
		}
	}

	/**
	 * Retrieve a connection, and execute the given read-only user transaction
	 * w/ a transaction isolation level of READ_UNCOMMITTED.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> T withReadUncommitted(final ServerTransaction<T> t) {
		try {
			return with(readUncommitted(), t);
		} catch (final SQLException e) {
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
	public static <T> T withReadOnly(final DBQuery<T> t) {
		try {
			return with(readOnly(), t);
		} catch (final SQLException e) {
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
	public static <T> T withReadOnly(final ServerQuery<T> t) {
		try {
			return with(readOnly(), t);
		} catch (final SQLException e) {
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
	public static <T> T withReadOnly(final ServerTransaction<T> t) {
		try {
			return with(readOnly(), t);
		} catch (final SQLException e) {
			throw new TransactionException(e);
		}
	}

	/**
	 * Retrieve a connection, and execute the given user transaction.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> T withUserTransaction(final UserQuery<T> t) {
		try {
			return withUser(userTransaction(), t);
		} catch (final SQLException e) {
			throw new TransactionException(e);
		}
	}

	/**
	 * Retrieve a connection, and execute the given user transaction.
	 * 
	 * @param <T>
	 * @param t
	 * @return
	 */
	public static <T> T withUserTransaction(final UserTransaction<T> t) {
		try {
			return withUser(userTransaction(), t);
		} catch (final SQLException e) {
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
	public static <T> T withUserReadOnly(final UserQuery<T> t) {
		try {
			return withUser(userReadOnly(), t);
		} catch (final SQLException e) {
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
	public static <T> T withUserReadOnly(final UserTransaction<T> t) {
		try {
			return withUser(userReadOnly(), t);
		} catch (final SQLException e) {
			throw new TransactionException(e);
		}
	}

	private static <T> T withUser(final UserConnection server,
			final UserQuery<T> t) {
		RuntimeException exc = null;
		try {
			return server.perform(t);
		} catch (final RuntimeException exc0) {
			exc = exc0;
		} finally {
			try {
				server.finished();
			} catch (final SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		throw exc;
	}

	private static <T> T withUser(final UserConnection server,
			final UserTransaction<T> t) {
		RuntimeException exc = null;
		try {
			return server.perform(t);
		} catch (final RuntimeException exc0) {
			exc = exc0;
		} finally {
			try {
				server.finished();
			} catch (final SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		throw exc;
	}

	private static <T> T with(final ServerConnection server, final DBQuery<T> t) {
		RuntimeException exc = null;
		try {
			return server.perform(t);
		} catch (final RuntimeException exc0) {
			exc = exc0;
		} finally {
			try {
				server.finished();
			} catch (final SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		throw exc;
	}

	private static <T> T with(final ServerConnection server,
			final DBTransaction<T> t) {
		RuntimeException exc = null;
		try {
			return server.perform(t);
		} catch (final RuntimeException exc0) {
			exc = exc0;
		} finally {
			try {
				server.finished();
			} catch (final SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		throw exc;
	}

	private static <T> T with(final ServerConnection server,
			final ServerQuery<T> t) {
		RuntimeException exc = null;
		try {
			return server.perform(t);
		} catch (final RuntimeException exc0) {
			exc = exc0;
		} finally {
			try {
				server.finished();
			} catch (final SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		throw exc;
	}

	private static <T> T with(final ServerConnection server,
			final ServerTransaction<T> t) {
		RuntimeException exc = null;
		try {
			return server.perform(t);
		} catch (final RuntimeException exc0) {
			exc = exc0;
		} finally {
			try {
				server.finished();
			} catch (final SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		throw exc;
	}

	private static User lookupUser() {
		final User user = UserContext.peek();
		if (user == null) {
			throw new IllegalStateException("There must be a user in context.");
		}
		return user;
	}

	/*
	 * Look up the JDBC data source.
	 */
	private static Connection lookupConnection() throws SQLException {
		try {
			final InitialContext context = new InitialContext();
			try {
				return ((DataSource) new InitialContext().lookup("jdbc/Sierra"))
						.getConnection();
			} finally {
				context.close();
			}
		} catch (final NamingException e) {
			throw new IllegalStateException(e);
		}
	}

	/*
	 * Look up the transaction handler.
	 */
	private static ExecutorService lookupExecutor() {
		try {
			final InitialContext context = new InitialContext();
			try {
				return (ExecutorService) new InitialContext()
						.lookup("SierraTransactionHandler");
			} finally {
				context.close();
			}
		} catch (final NamingException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Returns the server's timer service.
	 * 
	 * @return
	 */
	public static ScheduledExecutorService lookupTimerService() {
		try {
			final InitialContext context = new InitialContext();
			try {
				return (ScheduledExecutorService) new InitialContext()
						.lookup("SierraTimerService");
			} finally {
				context.close();
			}
		} catch (final NamingException e) {
			throw new IllegalStateException(e);
		}
	}

}

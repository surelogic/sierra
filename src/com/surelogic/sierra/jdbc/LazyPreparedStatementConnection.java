package com.surelogic.sierra.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * LazyPreparedStatementConnection proxies Connection and supplies slightly
 * different (but still valid) behavior than a normal Connection. Essentially, a
 * PreparedStatement is not actually created until someone attempts to invoke a
 * method on it. In addition, all PreparedStatement objects are closed when a
 * Connection is closed.
 * 
 * @author nathan
 * 
 */
public class LazyPreparedStatementConnection implements InvocationHandler {

	private final Connection conn;
	private final List<PreparedStatement> statements;

	public LazyPreparedStatementConnection(Connection conn) {
		this.conn = conn;
		this.statements = new ArrayList<PreparedStatement>();
	}

	public static Connection wrap(Connection conn) {
		return (Connection) Proxy.newProxyInstance(Connection.class
				.getClassLoader(), new Class[] { Connection.class },
				new LazyPreparedStatementConnection(conn));
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if ("prepareStatement".equals(method.getName())) {
			return Proxy.newProxyInstance(PreparedStatement.class
					.getClassLoader(), new Class[] { PreparedStatement.class },
					new LazyPreparedStatement(method, args));
		} else if ("close".equals(method.getName())) {
			for (PreparedStatement st : statements) {
				if (!st.isClosed()) {
					st.close();
				}
			}
		}
		try {
			return method.invoke(conn, args);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof Exception) {
				throw (Exception) target;
			} else {
				throw e;
			}
		}
	}

	private class LazyPreparedStatement implements InvocationHandler {

		private final Callable<PreparedStatement> init;
		private PreparedStatement st;

		public LazyPreparedStatement(final Method method, final Object... args) {
			init = new Callable<PreparedStatement>() {

				public PreparedStatement call() throws Exception {
					try {
						PreparedStatement st = (PreparedStatement) method
								.invoke(conn, args);
						statements.add(st);
						return st;
					} catch (InvocationTargetException e) {
						Throwable target = e.getTargetException();
						if (target instanceof Exception) {
							throw (Exception) target;
						} else {
							throw e;
						}
					}
				}

			};
		}

		void check() throws Exception {
			if (st == null) {
				st = init.call();
			}
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			check();
			try {
				return method.invoke(st, args);
			} catch (InvocationTargetException e) {
				Throwable target = e.getTargetException();
				if (target instanceof Exception) {
					throw (Exception) target;
				} else {
					throw e;
				}
			}
		}

	}
}

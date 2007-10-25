package com.surelogic.sierra.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.Callable;

public class LazyPreparedStatementConnection implements InvocationHandler {

	private final Connection conn;

	public LazyPreparedStatementConnection(Connection conn) {
		this.conn = conn;
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
						return (PreparedStatement) method.invoke(conn, args);
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

package com.surelogic.sierra.jdbc;

import java.sql.Connection;

public class Queries {

	private Queries() {
		// Not instantiable
	}

	public static Query create(Connection conn) {
		return new ConnectionQuery(conn);
	}

	public static <T> T with(Queryable<T> q, Object... args) {
		try {
			return q.call(args);
		} finally {
			q.finished();
		}
	}

	public static <T> T with(Queryable<T> q) {
		try {
			return q.call();
		} finally {
			q.finished();
		}
	}
}

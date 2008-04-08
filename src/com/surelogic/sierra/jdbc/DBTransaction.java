package com.surelogic.sierra.jdbc;

import java.sql.Connection;

public interface DBTransaction<T> {
	T perform(Connection conn) throws Exception;
}

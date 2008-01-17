package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents an sql transaction.
 * 
 * @author nathan
 * 
 * @param <T>
 */
public interface Transaction<T> {

	T perform(Connection conn, Server server) throws SQLException;

}

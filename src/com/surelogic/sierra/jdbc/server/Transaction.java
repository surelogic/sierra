package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;

/**
 * Represents an sql transaction.
 * 
 * @author nathan
 * 
 * @param <T>
 */
public interface Transaction<T> {

	T perform(Connection conn, Server server) throws Exception;

}

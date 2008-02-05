package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;

/**
 * Represents a scheduled/administrative transaction that occurs without a
 * particular user in context.
 * 
 * @author nathan
 * 
 */
public interface ServerTransaction<T> {

	T perform(Connection conn, Server server) throws Exception;

}

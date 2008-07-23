package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;

import com.surelogic.sierra.jdbc.user.User;

/**
 * Represents an SQL transaction with a principal in context.
 * 
 * @author nathan
 * 
 */
public interface UserTransaction<T> {

	T perform(Connection conn, Server server, User user) throws Exception;

}

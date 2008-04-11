package com.surelogic.sierra.jdbc;

import java.sql.Connection;

/**
 * A helper implementation of {@link DBTransaction} that produces no result.
 * 
 * @author nathan
 * 
 */
public abstract class DBTransactionEmpty implements DBTransaction<Void> {

	public Void perform(Connection conn) throws Exception {
		doPerform(conn);
		return null;
	}

	abstract public void doPerform(Connection conn);

}

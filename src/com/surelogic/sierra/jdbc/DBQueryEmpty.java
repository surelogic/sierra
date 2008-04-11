package com.surelogic.sierra.jdbc;

/**
 * A helper implementation of {@link DBQuery} that returns no result.
 * 
 * @author nathan
 * 
 */
public abstract class DBQueryEmpty implements DBQuery<Void> {

	public Void perform(Query q) {
		doPerform(q);
		return null;
	}

	abstract public void doPerform(Query q);

}

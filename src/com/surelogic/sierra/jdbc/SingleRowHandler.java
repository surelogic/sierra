package com.surelogic.sierra.jdbc;

/**
 * This {@link ResultHandler} produces null if now row exists, and otherwise
 * returns the value of {@code handleRow}.
 * 
 * @author nathan
 * 
 * @param <T>
 */
public abstract class SingleRowHandler<T> implements ResultHandler<T> {

	public T handle(Result r) {
		for (final Row row : r) {
			return handleRow(row);
		}
		return null;
	}

	public abstract T handleRow(Row r);

}

package com.surelogic.sierra.jdbc;

public interface Queryable<T> {

	T call(Object... args);

	T call();
	
	void finished();
}

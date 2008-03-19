package com.surelogic.sierra.jdbc;

public interface RowHandler<T> {

	T handle(Row r);
	
}

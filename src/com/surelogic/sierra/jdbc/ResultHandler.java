package com.surelogic.sierra.jdbc;

public interface ResultHandler<T> {

	T handle(Result r);
	
}

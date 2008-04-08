package com.surelogic.sierra.jdbc;



public interface DBQuery<T> {
	T perform(Query q);
}

package com.surelogic.sierra.jdbc;

import java.sql.Connection;
import java.util.List;

import com.surelogic.sierra.jdbc.qrecord.Record;

/**
 * Classes that implement query can return {@link Queryable} objects.
 * 
 * @author nathan
 * 
 */
public interface Query {

	Queryable<Void> prepared(String key);

	<T> Queryable<List<T>> prepared(String key, RowHandler<T> rh);

	<T> Queryable<T> prepared(String key, ResultHandler<T> rh);

	Queryable<Void> statement(String key);

	<T> Queryable<T> statement(String key, ResultHandler<T> rh);

	<T> Queryable<List<T>> statement(String key, RowHandler<T> rh);

	<T extends Record<?>> T record(Class<T> record);

	Connection getConnection();
}

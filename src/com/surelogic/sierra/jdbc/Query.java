package com.surelogic.sierra.jdbc;

import java.util.List;

import com.surelogic.sierra.jdbc.qrecord.Record;

/**
 * Classes that implement query can return {@link Queryable} objects by a given
 * key. The existing implementation of Query is {@link ConnectionQuery}.
 * 
 * @author nathan
 * 
 */
public interface Query {
	/**
	 * Return a Queryable object representing the prepared statement stored at
	 * the provided key.
	 * 
	 * @param key
	 * @return
	 */
	Queryable<Void> prepared(String key);

	/**
	 * Return a Queryable object representing the prepared statement stored at
	 * the provided key that will return the result of evaluating the provided
	 * {@link RowHandler} against each row when called.
	 * 
	 * @param <T>
	 * @param key
	 * @param rh
	 * @return
	 */
	<T> Queryable<List<T>> prepared(String key, RowHandler<T> rh);

	/**
	 * Return a Queryable object representing the prepared statement stored at
	 * the provided key that will return the result of evaluating the provided
	 * {@link ResultHandler} against the full result set when called.
	 * 
	 * @param <T>
	 * @param key
	 * @param rh
	 * @return
	 */
	<T> Queryable<T> prepared(String key, ResultHandler<T> rh);

	/**
	 * Return a Queryable object representing the statement stored at the
	 * provided key.
	 * 
	 * @param key
	 * @return
	 */
	Queryable<Void> statement(String key);

	/**
	 * Return a Queryable object representing the statement stored at the
	 * provided key that will return the result of evaluating the provided
	 * {@link RowHandler} against each row when called.
	 * 
	 * @param <T>
	 * @param key
	 * @param rh
	 * @return
	 */
	<T> Queryable<List<T>> statement(String key, RowHandler<T> rh);

	/**
	 * Return a Queryable object representing the prepared statement stored at
	 * the provided key that will return the result of evaluating the provided
	 * {@link ResultHandler} against the full result set when called.
	 * 
	 * @param <T>
	 * @param key
	 * @param rh
	 * @return
	 */
	<T> Queryable<T> statement(String key, ResultHandler<T> rh);

	/**
	 * Return an instance of the given record, backed by this Query store. See
	 * {@link ConnectionQuery#record(Class)} for details on how this works for a
	 * JDBC connection.
	 * 
	 * @param <T>
	 * @param record
	 * @return
	 */
	<T extends Record<?>> T record(Class<T> record);
}

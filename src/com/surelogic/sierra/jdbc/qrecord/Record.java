package com.surelogic.sierra.jdbc.qrecord;

import java.sql.SQLException;

import com.surelogic.sierra.jdbc.ConnectionQuery;

/**
 * Represents a record on a table. Records have a primary key, and can be
 * inserted into the table and deleted from a table by it. Record's also have a
 * natural key, which is generally specific to the type of data stored in the
 * record. They can be selected by their natural key.
 * 
 * @author nathan
 * 
 * @param <T>
 * @see ConnectionQuery#record(Class)
 */
public interface Record<T> {

	/**
	 * Return the primary key of the record.
	 * 
	 * @return the type of the record's primary key, or {@code null} if this is
	 *         a new record
	 */
	T getId();

	/**
	 * Set the primary key of the record.
	 * 
	 * @param id
	 */
	void setId(T id);

	/**
	 * Insert this record into the database.
	 * 
	 * @throws SQLException
	 */
	void insert();

	/**
	 * Select a record matching this one from the database by the record's
	 * natural key. All attributes of the record are filled out, except for
	 * references to other records, which are NOT populated.
	 * 
	 * @throws SQLException
	 */
	boolean select();

	/**
	 * Delete a record matching this one from the database. Deletions only occur
	 * by an id, which must have already been set on the record.
	 * 
	 * @throws SQLException
	 */
	void delete();
}

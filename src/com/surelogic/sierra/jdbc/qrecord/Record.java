package com.surelogic.sierra.jdbc.qrecord;

import java.sql.SQLException;

public interface Record<T> {

	/**
	 * Return the primary key of the record.
	 * 
	 * @return the type of the record's primary key, or null this is a new
	 *         record
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

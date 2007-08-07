/**
 * 
 */
package com.surelogic.sierra.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a record that can be written to the database.
 * 
 * @author nathan
 * 
 * @param <T>
 *            the type of this record's primary key
 */
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
	 * Fill a statement with all fields that are written to this record on the
	 * database. This may not include the record's primary key, depending on
	 * whether or not it is generated.
	 * 
	 * @param st
	 * @param idx
	 *            the statement's current index value
	 * @return the next index value for this prepared statement
	 * @throws SQLException
	 */
	int fill(PreparedStatement st, int idx) throws SQLException;

	/**
	 * Fill a statement with the record's primary key.
	 * 
	 * @param st
	 * @param idx
	 *            the statement's current index value
	 * @return the next index value for this prepared statement
	 * @throws SQLException
	 */
	int fillWithPk(PreparedStatement st, int idx) throws SQLException;
	
	/**
	 * Read the record's primary key from a result set.
	 * 
	 * @param set
	 * @param idx
	 *            the statement's current index value
	 * @return the next index value for this prepared statement
	 * @throws SQLException
	 */
	int readPk(ResultSet set, int idx) throws SQLException;

}
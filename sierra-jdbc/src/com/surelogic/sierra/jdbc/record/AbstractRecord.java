package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractRecord<T> implements Record<T> {

	private final RecordMapper mapper;

	protected AbstractRecord(RecordMapper mapper) {
		this.mapper = mapper;
	}

	public void delete() throws SQLException {
		mapper.remove(this);
	}

	public void insert() throws SQLException {
		mapper.insert(this);
	}

	public boolean select() throws SQLException {
		return mapper.select(this);

	}

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
	protected abstract int fill(PreparedStatement st, int idx)
			throws SQLException;

	/**
	 * Fill a statement with the record's natural key.
	 * 
	 * @param st
	 * @param idx
	 *            the statement's current index value
	 * @return the next index value for this prepared statement
	 * @throws SQLException
	 */
	protected abstract int fillWithNk(PreparedStatement st, int idx)
			throws SQLException;

	/**
	 * Fill a statement with the record's primary key.
	 * 
	 * @param st
	 * @param idx
	 *            the statement's current index value
	 * @return the next index value for this prepared statement
	 * @throws SQLException
	 */
	protected abstract int fillWithPk(PreparedStatement st, int idx)
			throws SQLException;

	/**
	 * Read the record's primary key from a result set.
	 * 
	 * @param set
	 * @param idx
	 *            the statement's current index value
	 * @return the next index value for this prepared statement
	 * @throws SQLException
	 */
	protected abstract int readPk(ResultSet set, int idx) throws SQLException;

	/**
	 * Read any attributes unrelated to the record's primary or merge keys.
	 * 
	 * @param set
	 * @param idx
	 *            the statement's current index value
	 * @return the next index value for this prepared statement
	 * @throws SQLException
	 */
	protected abstract int readAttributes(ResultSet set, int idx)
			throws SQLException;

}

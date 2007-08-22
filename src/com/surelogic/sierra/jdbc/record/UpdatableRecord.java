package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class UpdatableRecord<R> extends AbstractRecord<R> {

	private final UpdateRecordMapper mapper;

	protected UpdatableRecord(UpdateRecordMapper mapper) {
		super(mapper);
		this.mapper = mapper;
	}

	public void update() throws SQLException {
		mapper.update(this);
	}

	/**
	 * Fill a statement with the record's primary key.
	 * 
	 * @param st
	 * @param idx
	 *            the statement's current index value
	 * @return the next index value for this prepared statement
	 * @throws SQLException
	 */
	protected abstract int fillWithAttributes(PreparedStatement st, int idx)
			throws SQLException;
}

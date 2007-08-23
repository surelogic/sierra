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
	 * Fill a statement with the updatable fields from this record
	 * 
	 * @param st
	 * @param idx
	 *            the statement's current index value
	 * @return the next index value for this prepared statement
	 * @throws SQLException
	 */
	protected abstract int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException;
}

package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A record that has a generate primary key with a long value may extend
 * LongRecord.
 * 
 * @author nathan
 * 
 */
public abstract class LongRecord extends AbstractRecord<Long> {

	protected Long id;

	protected LongRecord(RecordMapper mapper) {
		super(mapper);
	}

	public Long getId() {
		return id;
	}

	@Override
	protected int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, id);
		return idx;
	}

	@Override
	protected int readPk(ResultSet set, int idx) throws SQLException {
		this.id = set.getLong(idx++);
		return idx;
	}

	public void setId(Long id) {
		this.id = id;
	}

}

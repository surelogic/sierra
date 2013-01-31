package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class LongUpdatableRecord extends AbstractUpdatableRecord<Long> {

	protected Long id;

	protected LongUpdatableRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	@Override
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

	@Override
  public void setId(Long id) {
		this.id = id;
	}

}

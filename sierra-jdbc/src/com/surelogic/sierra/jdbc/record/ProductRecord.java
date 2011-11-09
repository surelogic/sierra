package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ProductRecord extends LongUpdatableRecord {

	private String name;

	public ProductRecord(UpdateBaseMapper mapper) {
		super(mapper);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		return idx;
	}

	@Override
	protected int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		st.setString(idx++, name);
		return idx;
	}

}

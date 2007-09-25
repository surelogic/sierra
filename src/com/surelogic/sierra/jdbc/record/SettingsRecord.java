package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsRecord extends LongRecord {

	private String name;
	private Long revision;

	public SettingsRecord(RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		st.setLong(idx++, revision);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		revision = set.getLong(idx++);
		return idx;
	}

}

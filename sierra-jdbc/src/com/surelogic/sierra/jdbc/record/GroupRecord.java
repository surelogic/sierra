package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupRecord extends LongUpdatableRecord {

	private String name;
	private String info;

	public GroupRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		st.setString(idx++, name);
		st.setString(idx++, info);
		return idx;
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		st.setString(idx++, info);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		this.info = set.getString(idx++);
		return idx;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String description) {
		this.info = description;
	}

}

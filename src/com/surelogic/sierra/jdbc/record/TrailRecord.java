package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableString;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class TrailRecord extends LongRecord {
	private String uid;
	private Long projectId;

	public TrailRecord(RecordMapper mapper) {
		super(mapper);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, projectId);
		setNullableString(idx++, st, uid);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		return fillWithPk(st, idx);
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		projectId = set.getLong(idx++);
		uid = set.getString(idx++);
		return idx;
	}

}

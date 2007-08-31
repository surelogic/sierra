package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableInt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.Importance;

public final class FindingRecord extends LongRecord {

	private Importance importance;
	private String uid;
	private Long projectId;

	public FindingRecord(RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, projectId);
		st.setString(idx++, uid);
		setNullableInt(idx++, st, importance.ordinal());
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		return fillWithPk(st, idx);
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		this.projectId = set.getLong(idx++);
		this.uid = set.getString(idx++);
		this.importance = Importance.values()[set.getInt(idx++)];
		return idx;
	}

	public Importance getImportance() {
		return importance;
	}

	public void setImportance(Importance importance) {
		this.importance = importance;
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

}

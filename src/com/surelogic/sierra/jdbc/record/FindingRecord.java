package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableInt;
import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableString;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.Importance;

public final class FindingRecord extends LongRecord {

	private String uid;
	private Long projectId;
	private Importance importance;
	private String summary;
	private boolean read;

	public FindingRecord(RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, projectId);
		st.setString(idx++, uid);
		setNullableInt(idx++, st, importance == null ? null : importance
				.ordinal());
		setNullableString(idx++, st, summary);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, uid);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		this.projectId = set.getLong(idx++);
		this.importance = Importance.values()[set.getInt(idx++)];
		this.summary = set.getString(idx++);
		this.read = "Y".equals(set.getString(idx++));
		return idx;
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

	public Importance getImportance() {
		return importance;
	}

	public void setImportance(Importance importance) {
		this.importance = importance;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public boolean isRead() {
		return read;
	}

}

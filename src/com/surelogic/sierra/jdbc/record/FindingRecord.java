package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableInt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.Importance;

public final class FindingRecord extends LongRecord {

	private boolean read;
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
		st.setString(idx++, read ? "Y" : "N");
		setNullableInt(idx++, st, importance.ordinal());
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
		this.read = set.getString(idx++).equals("Y");
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

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

}

package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.*;
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
	private Long obsoletedById;
	private Long obsoletedByRevision;

	public FindingRecord(RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, projectId);
		st.setString(idx++, uid);
		st.setInt(idx++, importance.ordinal());
		st.setString(idx++, summary);
		setNullableLong(idx++, st, obsoletedById);
		setNullableLong(idx++, st, obsoletedByRevision);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, uid);
		return idx;
	}

	@Override
	public int readAttributes(ResultSet set, int idx) throws SQLException {
		this.projectId = set.getLong(idx++);
		this.importance = Importance.values()[set.getInt(idx++)];
		this.summary = set.getString(idx++);
		this.read = "Y".equals(set.getString(idx++));
		this.obsoletedById = getNullableLong(idx++, set);
		this.obsoletedByRevision = getNullableLong(idx++, set);
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

	public Long getObsoletedById() {
		return obsoletedById;
	}

	public void setObsoletedByUid(Long obsoletedById) {
		this.obsoletedById = obsoletedById;
	}

	public Long getObsoletedByRevision() {
		return obsoletedByRevision;
	}

	public void setObsoletedByRevision(Long obsoletedByRevision) {
		this.obsoletedByRevision = obsoletedByRevision;
	}

}

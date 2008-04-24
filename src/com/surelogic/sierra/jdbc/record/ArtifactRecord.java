package com.surelogic.sierra.jdbc.record;

import static com.surelogic.common.jdbc.JDBCUtils.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public final class ArtifactRecord extends LongRecord {

	private Long scanId;
	private Long findingTypeId;
	private Priority priority;
	private Severity severity;
	private String message;
	private SourceRecord primary;

	public ArtifactRecord(RecordMapper mapper) {
		super(mapper);
	}

	public Long getScanId() {
		return scanId;
	}

	public void setScanId(Long scanId) {
		this.scanId = scanId;
	}

	public Long getArtifactTypeId() {
		return findingTypeId;
	}

	public void setFindingTypeId(Long findingTypeId) {
		this.findingTypeId = findingTypeId;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public SourceRecord getPrimary() {
		return primary;
	}

	public void setPrimary(SourceRecord primary) {
		this.primary = primary;
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, scanId);
		st.setLong(idx++, findingTypeId);
		st.setLong(idx++, primary.getId());
		setNullableInt(idx++, st, priority == null ? null : priority.ordinal());
		setNullableInt(idx++, st, severity == null ? null : severity.ordinal());
		setNullableString(idx++, st, message);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		scanId = set.getLong(idx++);
		findingTypeId = set.getLong(idx++);
		priority = Priority.values()[set.getInt(idx++)];
		severity = Severity.values()[set.getInt(idx++)];
		message = set.getString(idx++);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		return fillWithPk(st, idx);
	}

}
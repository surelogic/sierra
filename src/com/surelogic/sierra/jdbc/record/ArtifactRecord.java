package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableString;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public final class ArtifactRecord extends LongRecord {

	private Long runId;
	private Long findingTypeId;
	private Priority priority;
	private Severity severity;
	private String message;
	private SourceRecord primary;

	public ArtifactRecord(RecordMapper mapper) {
		super(mapper);
	}

	public Long getRunId() {
		return runId;
	}

	public void setRunId(Long runId) {
		this.runId = runId;
	}

	public Long getFindingTypeId() {
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
		st.setLong(idx++, runId);
		st.setLong(idx++, findingTypeId);
		st.setLong(idx++, primary.getId());
		st.setInt(idx++, priority.ordinal());
		st.setInt(idx++, severity.ordinal());
		setNullableString(idx++, st, message);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		runId = set.getLong(idx++);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (findingTypeId ^ (findingTypeId >>> 32));
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((primary == null) ? 0 : primary.hashCode());
		result = prime * result
				+ ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + (int) (runId ^ (runId >>> 32));
		result = prime * result
				+ ((severity == null) ? 0 : severity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ArtifactRecord other = (ArtifactRecord) obj;
		if (findingTypeId != other.findingTypeId)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (primary == null) {
			if (other.primary != null)
				return false;
		} else if (!primary.equals(other.primary))
			return false;
		if (priority == null) {
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
			return false;
		if (runId != other.runId)
			return false;
		if (severity == null) {
			if (other.severity != null)
				return false;
		} else if (!severity.equals(other.severity))
			return false;
		return true;
	}

}
package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.surelogic.sierra.tool.message.AuditEvent;

public final class AuditRecord extends LongRecord {

	private Long userId;
	private Long trailId;
	private Date timestamp;
	private String value;
	private AuditEvent event;
	private Long revision;

	AuditRecord(RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		// USER_ID, TRAIL_ID, DATE_TIME, VALUE, EVENT
		st.setLong(idx++, userId);
		st.setLong(idx++, trailId);
		st.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
		st.setString(idx++, value);
		st.setInt(idx++, event.ordinal());
		return idx;
	}

	protected int fill(PreparedStatement st, int idx) throws SQLException {
		idx = fillWithNk(st, idx);
		st.setLong(idx++, revision);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		revision = set.getLong(idx++);
		return idx;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getTrailId() {
		return trailId;
	}

	public void setTrailId(Long trailId) {
		this.trailId = trailId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public AuditEvent getEvent() {
		return event;
	}

	public void setEvent(AuditEvent event) {
		this.event = event;
	}

	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result = prime * result
				+ ((revision == null) ? 0 : revision.hashCode());
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((trailId == null) ? 0 : trailId.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		final AuditRecord other = (AuditRecord) obj;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		if (revision == null) {
			if (other.revision != null)
				return false;
		} else if (!revision.equals(other.revision))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (trailId == null) {
			if (other.trailId != null)
				return false;
		} else if (!trailId.equals(other.trailId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}

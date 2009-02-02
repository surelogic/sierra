package com.surelogic.sierra.jdbc.record;

import static com.surelogic.common.jdbc.JDBCUtils.getNullableLong;
import static com.surelogic.common.jdbc.JDBCUtils.setNullableLong;
import static com.surelogic.common.jdbc.JDBCUtils.setNullableString;
import static com.surelogic.common.jdbc.JDBCUtils.setNullableTimestamp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.surelogic.sierra.tool.message.AuditEvent;

public final class AuditRecord extends AbstractRecord<String> {

	private String uuid;
	private Long userId;
	private Long findingId;
	private Date timestamp;
	private String value;
	private AuditEvent event;
	private Long revision;

	public AuditRecord(final RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fillWithNk(final PreparedStatement st, final int idx)
			throws SQLException {
		return fillWithPk(st, idx);
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(final Long userId) {
		this.userId = userId;
	}

	public Long getFindingId() {
		return findingId;
	}

	public void setFindingId(final Long findingId) {
		this.findingId = findingId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public AuditEvent getEvent() {
		return event;
	}

	public void setEvent(final AuditEvent event) {
		this.event = event;
	}

	public Long getRevision() {
		return revision;
	}

	public void setRevision(final Long revision) {
		this.revision = revision;
	}

	@Override
	protected int fill(final PreparedStatement st, int idx) throws SQLException {
		idx = fillWithPk(st, idx);
		st.setLong(idx++, findingId);
		st.setString(idx++, event.toString());
		setNullableLong(idx++, st, userId);
		setNullableTimestamp(idx++, st, timestamp);
		setNullableString(idx++, st, value);
		setNullableLong(idx++, st, revision);
		return idx;
	}

	@Override
	protected int fillWithPk(final PreparedStatement st, int idx)
			throws SQLException {
		st.setString(idx++, uuid);
		return idx;
	}

	@Override
	protected int readAttributes(final ResultSet set, int idx)
			throws SQLException {
		findingId = set.getLong(idx++);
		event = AuditEvent.valueOf(set.getString(idx++));
		userId = getNullableLong(idx++, set);
		timestamp = set.getTimestamp(idx++);
		value = set.getString(idx++);
		revision = getNullableLong(idx++, set);
		return idx;
	}

	@Override
	protected int readPk(final ResultSet set, int idx) throws SQLException {
		uuid = set.getString(idx++);
		return idx;
	}

	public String getId() {
		return uuid;
	}

	public void setId(final String id) {
		uuid = id;
	}

}

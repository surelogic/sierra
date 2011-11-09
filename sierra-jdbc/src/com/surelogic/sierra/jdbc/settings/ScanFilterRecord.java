package com.surelogic.sierra.jdbc.settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.common.jdbc.UpdateRecordMapper;
import com.surelogic.sierra.jdbc.qrecord.LongUpdatableRecord;

public class ScanFilterRecord extends LongUpdatableRecord {

	private String name;
	private String uid;
	private Long revision;

	public ScanFilterRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, uid);
		st.setString(idx++, name);
		st.setLong(idx++, revision);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, uid);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		name = set.getString(idx++);
		revision = set.getLong(idx++);
		return idx;
	}

	@Override
	protected int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		st.setLong(idx++, revision);
		st.setString(idx++, name);
		return idx;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if ((name == null) || (name.length() > 255)) {
			throw new IllegalArgumentException(
					"The name "
							+ name
							+ " cannot be null or longer than 255 characters in length.");
		}
		this.name = name;
	}

	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}

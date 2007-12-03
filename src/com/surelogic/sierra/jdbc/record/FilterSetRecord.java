package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableString;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FilterSetRecord extends LongUpdatableRecord {

	private String uid;
	private String name;
	private String info;
	private long revision;

	public FilterSetRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		st.setString(idx++, name);
		st.setLong(idx++, revision);
		setNullableString(idx++, st, info);
		return idx;
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, uid);
		st.setLong(idx++, revision);
		st.setString(idx++, name);
		setNullableString(idx++, st, info);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, uid);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		this.revision = set.getLong(idx++);
		this.name = set.getString(idx++);
		this.info = set.getString(idx++);
		return idx;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}

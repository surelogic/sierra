package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static com.surelogic.sierra.jdbc.JDBCUtils.*;

public class FilterSetRecord extends LongUpdatableRecord {

	private String uid;
	private String name;
	private String info;

	public FilterSetRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		return fill(st, idx);
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, uid);
		st.setString(idx++, name);
		st.setString(idx++, info);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, uid);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
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

	public String getUid() {
		return uid;
	}

}

package com.surelogic.sierra.jdbc.finding;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableString;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.LongRecord;

public class TrailRecord extends LongRecord {
	private String uid;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int fill(PreparedStatement st, int idx) throws SQLException {
		setNullableString(idx++, st, uid);
		return idx;
	}
}

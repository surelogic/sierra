package com.surelogic.sierra.jdbc.finding;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableString;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.LongRecord;

public class TrailRecord extends LongRecord {
	private String uid;
	private Long projectId;

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

	public int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, projectId);
		setNullableString(idx++, st, uid);
		return idx;
	}
}

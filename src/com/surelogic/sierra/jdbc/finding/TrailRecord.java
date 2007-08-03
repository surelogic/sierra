package com.surelogic.sierra.jdbc.finding;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.Record;
import static com.surelogic.sierra.jdbc.JDBCUtils.*;

public class TrailRecord implements Record<Long> {
	private Long id;
	private String uid;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

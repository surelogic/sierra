package com.surelogic.sierra.jdbc.finding;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableInt;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.LongRecord;
import com.surelogic.sierra.tool.message.Importance;

public class FindingRecord extends LongRecord {

	private TrailRecord trail;
	private Importance importance;

	public int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, trail.getId());
		setNullableInt(idx++, st, importance.ordinal());
		return idx;
	}

	public TrailRecord getTrail() {
		return trail;
	}

	public void setTrail(TrailRecord trail) {
		this.trail = trail;
	}

	public Importance getImportance() {
		return importance;
	}

	public void setImportance(Importance importance) {
		this.importance = importance;
	}

}

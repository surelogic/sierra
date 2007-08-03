package com.surelogic.sierra.jdbc.finding;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.Record;
import com.surelogic.sierra.tool.message.Importance;
import static com.surelogic.sierra.jdbc.JDBCUtils.*;

public class FindingRecord implements Record<Long> {

	private Long id;
	private TrailRecord trail;
	private Importance importance;

	public int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, trail.getId());
		setNullableInt(idx++, st, importance.ordinal());
		return idx;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

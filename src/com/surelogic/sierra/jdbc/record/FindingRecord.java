package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableInt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.Importance;

public final class FindingRecord extends LongRecord {

	private TrailRecord trail;
	private Importance importance;

	public FindingRecord(RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, trail.getId());
		setNullableInt(idx++, st, importance.ordinal());
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		return fillWithPk(st, idx);
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		this.importance = Importance.values()[set.getInt(idx++)];
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

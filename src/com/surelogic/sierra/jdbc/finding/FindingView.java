package com.surelogic.sierra.jdbc.finding;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.LongView;
import com.surelogic.sierra.tool.message.Importance;

public class FindingView extends LongView {

	private Long trailId;
	private Importance importance;

	protected int readRest(ResultSet set, int idx) throws SQLException {
		idx = super.read(set, idx);
		trailId = set.getLong(idx++);
		importance = Importance.values()[set.getInt(idx++)];
		return idx;
	}

	public Long getTrailId() {
		return trailId;
	}

	public Importance getImportance() {
		return importance;
	}

}

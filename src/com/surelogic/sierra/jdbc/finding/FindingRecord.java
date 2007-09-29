package com.surelogic.sierra.jdbc.finding;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.LongView;
import com.surelogic.sierra.tool.message.Importance;

public class FindingRecord extends LongView {

	private Importance importance;
	private boolean isRead;

	@Override
	protected int readRest(ResultSet set, int idx) throws SQLException {
		idx = super.read(set, idx);
		importance = Importance.values()[set.getInt(idx++)];
		isRead = "Y".equals(set.getString(idx++));
		return idx;
	}

	public Importance getImportance() {
		return importance;
	}

	public boolean isRead() {
		return isRead;
	}

}

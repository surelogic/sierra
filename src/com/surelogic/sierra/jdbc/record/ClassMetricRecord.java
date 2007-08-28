package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClassMetricRecord extends
		LongRecordRelationRecord<RunRecord, CompilationUnitRecord> {

	private Integer linesOfCode;

	public ClassMetricRecord(RecordMapper mapper) {
		super(mapper);
	}

	public Integer getLinesOfCode() {
		return linesOfCode;
	}

	public void setLinesOfCode(Integer linesOfCode) {
		this.linesOfCode = linesOfCode;
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		idx = super.fill(st, idx);
		st.setInt(idx++, linesOfCode);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		this.linesOfCode = set.getInt(idx++);
		return idx;
	}

}

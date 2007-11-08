package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ScanSummaryRecord extends UpdatableRecord<ScanSummaryRecord.PK> {

	public ScanSummaryRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	private PK id;

	private long newFindings;
	private long fixedFindings;
	private long unchangedFindings;
	private long artifacts;
	private long totalFindings;
	private long linesOfCode;

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		idx = fillWithNk(st, idx);
		st.setLong(idx++, newFindings);
		st.setLong(idx++, fixedFindings);
		st.setLong(idx++, unchangedFindings);
		st.setLong(idx++, artifacts);
		st.setLong(idx++, totalFindings);
		st.setLong(idx++, linesOfCode);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, id.scanId);
		st.setLong(idx++, id.qualifierId);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		newFindings = set.getLong(idx++);
		fixedFindings = set.getLong(idx++);
		unchangedFindings = set.getLong(idx++);
		artifacts = set.getLong(idx++);
		totalFindings = set.getLong(idx++);
		linesOfCode = set.getLong(idx++);
		return idx;
	}

	@Override
	protected int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		st.setLong(idx++, newFindings);
		st.setLong(idx++, fixedFindings);
		st.setLong(idx++, unchangedFindings);
		st.setLong(idx++, artifacts);
		st.setLong(idx++, totalFindings);
		st.setLong(idx++, linesOfCode);
		return idx;
	}

	public long getTotalFindings() {
		return totalFindings;
	}

	public void setTotalFindings(long totalFindings) {
		this.totalFindings = totalFindings;
	}

	public long getNewFindings() {
		return newFindings;
	}

	public void setNewFindings(long newFindings) {
		this.newFindings = newFindings;
	}

	public long getFixedFindings() {
		return fixedFindings;
	}

	public void setFixedFindings(long fixedFindings) {
		this.fixedFindings = fixedFindings;
	}

	public long getUnchangedFindings() {
		return unchangedFindings;
	}

	public void setUnchangedFindings(long unchangedFindings) {
		this.unchangedFindings = unchangedFindings;
	}

	public long getLinesOfCode() {
		return linesOfCode;
	}

	public void setLinesOfCode(long linesOfCode) {
		this.linesOfCode = linesOfCode;
	}

	public long getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(long artifacts) {
		this.artifacts = artifacts;
	}

	public static class PK {
		private long scanId;
		private long qualifierId;

		public PK() {
			// Do nothing
		}

		public PK(long scanId, long qualifierId) {
			this.scanId = scanId;
			this.qualifierId = qualifierId;
		}

		public long getScanId() {
			return scanId;
		}

		public void setScanId(long scanId) {
			this.scanId = scanId;
		}

		public long getQualifierId() {
			return qualifierId;
		}

		public void setQualifierId(long qualifierId) {
			this.qualifierId = qualifierId;
		}

	}

	@Override
	protected int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		return fillWithNk(st, idx);
	}

	@Override
	protected int readPk(ResultSet set, int idx) throws SQLException {
		return idx;
	}

	public PK getId() {

		return id;
	}

	public void setId(PK id) {
		this.id = id;
	}

}

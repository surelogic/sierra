package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ScanSummaryRecord extends UpdatableRecord<ScanSummaryRecord.PK> {

	public ScanSummaryRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	private PK id;

	private Long newFindings;
	private Long fixedFindings;
	private Long unchangedFindings;
	private Long artifacts;

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		idx = fillWithNk(st, idx);
		st.setLong(idx++, newFindings);
		st.setLong(idx++, fixedFindings);
		st.setLong(idx++, unchangedFindings);
		st.setLong(idx++, artifacts);
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
		return idx;
	}

	@Override
	protected int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		st.setLong(idx++, newFindings);
		st.setLong(idx++, fixedFindings);
		st.setLong(idx++, unchangedFindings);
		st.setLong(idx++, artifacts);
		return 0;
	}

	public Long getNewFindings() {
		return newFindings;
	}

	public void setNewFindings(Long newFindings) {
		this.newFindings = newFindings;
	}

	public Long getFixedFindings() {
		return fixedFindings;
	}

	public void setFixedFindings(Long fixedFindings) {
		this.fixedFindings = fixedFindings;
	}

	public Long getUnchangedFindings() {
		return unchangedFindings;
	}

	public void setUnchangedFindings(Long unchangedFindings) {
		this.unchangedFindings = unchangedFindings;
	}

	public Long getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(Long artifacts) {
		this.artifacts = artifacts;
	}

	public static class PK {
		private Long scanId;
		private Long qualifierId;

		public PK() {
			// Do nothing
		}

		public PK(Long scanId, Long qualifierId) {
			this.scanId = scanId;
			this.qualifierId = qualifierId;
		}

		public Long getScanId() {
			return scanId;
		}

		public void setScanId(Long scanId) {
			this.scanId = scanId;
		}

		public Long getQualifierId() {
			return qualifierId;
		}

		public void setQualifierId(Long qualifierId) {
			this.qualifierId = qualifierId;
		}

	}

	@Override
	protected int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		return fillWithPk(st, idx);
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

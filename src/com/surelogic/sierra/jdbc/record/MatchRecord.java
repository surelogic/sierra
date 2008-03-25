package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class MatchRecord extends AbstractUpdatableRecord<MatchRecord.PK> {

	private PK id;

	private Long findingId;

	private Long revision;

	public MatchRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		idx = fillWithPk(st, idx);
		setNullableLong(idx++, st, findingId);
		setNullableLong(idx++, st, revision);
		return idx;
	}

	@Override
	protected int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, id.getProjectId());
		st.setLong(idx++, id.getHash());
		st.setString(idx++, id.getClassName());
		st.setString(idx++, id.getPackageName());
		st.setLong(idx++, id.getFindingTypeId());
		return idx;
	}

	@Override
	protected int readPk(ResultSet set, int idx) throws SQLException {
		// pk is the same as the nk
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		this.findingId = getNullableLong(idx++, set);
		this.revision = getNullableLong(idx++, set);
		return 0;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		return fillWithPk(st, idx);
	}

	@Override
	protected int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		setNullableLong(idx++, st, findingId);
		setNullableLong(idx++, st, revision);
		return idx;
	}

	public PK getId() {
		return id;
	}

	public void setId(PK id) {
		this.id = id;
	}

	public Long getFindingId() {
		return findingId;
	}

	public void setFindingId(Long findingId) {
		this.findingId = findingId;
	}

	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}
	
	/**
	 * Represents a match record's primary key.
	 * 
	 * @author nathan
	 * 
	 */
	public static class PK {
		private Long projectId;
		private String className;
		private String packageName;
		private Long findingTypeId;
		private Long hash;

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		public Long getFindingTypeId() {
			return findingTypeId;
		}

		public void setFindingTypeId(Long findingTypeId) {
			this.findingTypeId = findingTypeId;
		}

		public Long getHash() {
			return hash;
		}

		public void setHash(Long hash) {
			this.hash = hash;
		}

		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((className == null) ? 0 : className.hashCode());
			result = prime * result
					+ ((findingTypeId == null) ? 0 : findingTypeId.hashCode());
			result = prime * result + ((hash == null) ? 0 : hash.hashCode());
			result = prime * result
					+ ((packageName == null) ? 0 : packageName.hashCode());
			result = prime * result
					+ ((projectId == null) ? 0 : projectId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final PK other = (PK) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			if (findingTypeId == null) {
				if (other.findingTypeId != null)
					return false;
			} else if (!findingTypeId.equals(other.findingTypeId))
				return false;
			if (hash == null) {
				if (other.hash != null)
					return false;
			} else if (!hash.equals(other.hash))
				return false;
			if (packageName == null) {
				if (other.packageName != null)
					return false;
			} else if (!packageName.equals(other.packageName))
				return false;
			if (projectId == null) {
				if (other.projectId != null)
					return false;
			} else if (!projectId.equals(other.projectId))
				return false;
			return true;
		}

	}

	@Override
	public String toString() {
		return "Match(" + id.projectId + "," + id.packageName + "."
				+ id.className + "," + id.hash + "," + id.findingTypeId + ")";
	}

}

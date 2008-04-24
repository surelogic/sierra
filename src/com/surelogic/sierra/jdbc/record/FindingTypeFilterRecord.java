package com.surelogic.sierra.jdbc.record;

import static com.surelogic.common.jdbc.JDBCUtils.setNullableInt;
import static com.surelogic.common.jdbc.JDBCUtils.setNullableString;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.Importance;

public class FindingTypeFilterRecord extends
		AbstractRecord<FindingTypeFilterRecord.PK> {

	public FindingTypeFilterRecord(RecordMapper mapper) {
		super(mapper);

	}

	private PK id;
	private Importance importance;
	private boolean filtered;

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		idx = fillWithPk(st, idx);
		final Integer imp = importance == null ? null : importance.ordinal();
		setNullableInt(idx++, st, imp);
		final String fil = filtered ? "Y" : null;
		setNullableString(idx++, st, fil);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		return fillWithPk(st, idx);
	}

	@Override
	protected int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, id.parentTableId);
		st.setLong(idx++, id.findingTypeId);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		boolean hasImportance;
		boolean hasFiltered;
		final int importance = set.getInt(idx++);
		hasImportance = !set.wasNull();
		if (hasImportance) {
			this.importance = Importance.values()[importance];
		} else {
			final String filtered = set.getString(idx++);
			hasFiltered = !set.wasNull();
			if (hasFiltered) {
				this.filtered = "Y".equals(filtered);
			}
		}
		return idx;
	}

	@Override
	protected int readPk(ResultSet set, int idx) throws SQLException {
		// The primary key is the same as the natural key
		return idx;
	}

	public PK getId() {
		return id;
	}

	public void setId(PK id) {
		this.id = id;
	}

	public static class PK {
		private Long parentTableId;
		private Long findingTypeId;

		public PK() {
			// Do Nothing
		}

		public PK(Long parentTableId, Long findingTypeId) {
			this.parentTableId = parentTableId;
			this.findingTypeId = findingTypeId;
		}

		public Long getParentTableId() {
			return parentTableId;
		}

		public void setParentTableId(Long parentTableId) {
			this.parentTableId = parentTableId;
		}

		public Long getFindingTypeId() {
			return findingTypeId;
		}

		public void setFindingTypeId(Long findingTypeId) {
			this.findingTypeId = findingTypeId;
		}

	}

	public Importance getImportance() {
		return importance;
	}

	public void setImportance(Importance importance) {
		this.importance = importance;
	}

	public Boolean getFiltered() {
		return filtered;
	}

	public void setFiltered(Boolean filtered) {
		this.filtered = filtered;
	}

}

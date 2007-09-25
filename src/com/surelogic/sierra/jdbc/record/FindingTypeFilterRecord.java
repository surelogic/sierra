package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.Importance;
import static com.surelogic.sierra.jdbc.JDBCUtils.*;
public class FindingTypeFilterRecord extends
		AbstractRecord<FindingTypeFilterRecord.PK> {

	public FindingTypeFilterRecord(RecordMapper mapper) {
		super(mapper);

	}

	private PK id;
	private Integer delta;
	private Importance importance;
	private Boolean filtered;

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		fillWithPk(st, idx);
		setNullableInt(idx++, st, delta);
		Integer imp = importance == null ? null : importance.ordinal();
		setNullableInt(idx++, st, imp);
		String fil = filtered == null ? null : (filtered ? "Y" : "N");
		setNullableString(idx++, st, fil);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, id.parentTableId);
		st.setLong(idx++, id.findingTypeId);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int readPk(ResultSet set, int idx) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public PK getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setId(PK id) {
		// TODO Auto-generated method stub

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

}

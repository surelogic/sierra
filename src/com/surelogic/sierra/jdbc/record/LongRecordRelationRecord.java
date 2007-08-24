package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LongRecordRelationRecord<R extends Record<Long>, S extends Record<Long>>
		extends RecordRelationRecord<R, S> {

	protected LongRecordRelationRecord(RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, id.a.getId());
		st.setLong(idx++, id.b.getId());
		return idx;
	}
}
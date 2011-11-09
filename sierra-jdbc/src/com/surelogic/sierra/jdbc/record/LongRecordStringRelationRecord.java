package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public class LongRecordStringRelationRecord<R extends Record<Long>>
		extends RecordStringRelationRecord<R> {

	protected LongRecordStringRelationRecord(RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, id.a.getId());
		st.setString(idx++, id.b);
		return idx;
	}

}

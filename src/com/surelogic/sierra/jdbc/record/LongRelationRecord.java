package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LongRelationRecord<R extends LongRecord, S extends LongRecord>
		extends RelationRecord<R, S> {

	protected LongRelationRecord(RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, id.a.getId());
		st.setLong(idx++, id.b.getId());
		return idx;
	}
}
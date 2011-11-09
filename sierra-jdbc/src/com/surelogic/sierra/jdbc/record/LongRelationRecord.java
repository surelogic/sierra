package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LongRelationRecord extends RelationRecord<Long, Long> {

	public LongRelationRecord(RecordMapper mapper) {
		super(mapper);
	}

	@Override
	protected int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, id.a);
		st.setLong(idx++, id.b);
		return idx;
	}

}

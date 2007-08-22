package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RunQualifierRecord extends
		LongRelationRecord<RunRecord, QualifierRecord> {

	public RunQualifierRecord(RecordMapper mapper) {
		super(mapper);
	}

}

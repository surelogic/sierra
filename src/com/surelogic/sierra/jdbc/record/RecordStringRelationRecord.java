package com.surelogic.sierra.jdbc.record;


public abstract class RecordStringRelationRecord<R extends Record<?>> extends
		RelationRecord<R, String> {

	protected RecordStringRelationRecord(RecordMapper mapper) {
		super(mapper);
	}

}

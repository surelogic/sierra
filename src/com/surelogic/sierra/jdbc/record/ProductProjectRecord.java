package com.surelogic.sierra.jdbc.record;


public final class ProductProjectRecord extends
		LongRecordRelationRecord<ProductRecord, ProjectRecord> {

	public ProductProjectRecord(RecordMapper mapper) {
		super(mapper);
	}

}

package com.surelogic.sierra.jdbc.record;


public final class ProductProjectRecord extends
		LongRelationRecord<ProductRecord, ProjectRecord> {

	public ProductProjectRecord(RecordMapper mapper) {
		super(mapper);
	}

}

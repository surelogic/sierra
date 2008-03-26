package com.surelogic.sierra.jdbc.qrecord;


public interface UpdateRecordMapper extends RecordMapper {

	public void update(AbstractUpdatableRecord<?> record);

}
package com.surelogic.sierra.jdbc.record;

import java.sql.SQLException;

public interface UpdateRecordMapper extends RecordMapper {

	public void update(AbstractUpdatableRecord<?> record) throws SQLException;

}
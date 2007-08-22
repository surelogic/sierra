package com.surelogic.sierra.jdbc.record;

import java.sql.SQLException;

//TODO I think we can enforce better type safety than this
public interface RecordMapper {

	public void insert(AbstractRecord<?> record) throws SQLException;

	public void remove(AbstractRecord<?> record) throws SQLException;

	public boolean select(AbstractRecord<?> record)
			throws SQLException;

}
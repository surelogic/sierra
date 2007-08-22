package com.surelogic.sierra.jdbc.record;

import java.sql.SQLException;

//TODO I think we can enforce better type safety than this
public interface RecordMapper {

	public abstract void insert(AbstractRecord<?> record) throws SQLException;

	public abstract void remove(AbstractRecord<?> record) throws SQLException;

	public abstract boolean select(AbstractRecord<?> record)
			throws SQLException;

}
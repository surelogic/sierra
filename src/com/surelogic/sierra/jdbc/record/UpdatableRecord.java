package com.surelogic.sierra.jdbc.record;

import java.sql.SQLException;

public interface UpdatableRecord<T> extends Record<T> {
	/**
	 * Update the record's row on the database.
	 * 
	 * @throws SQLException
	 */
	void update() throws SQLException;
}

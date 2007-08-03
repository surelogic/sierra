/**
 * 
 */
package com.surelogic.sierra.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Record<T> {

	T getId();

	void setId(T id);

	int fill(PreparedStatement st, int idx) throws SQLException;

}
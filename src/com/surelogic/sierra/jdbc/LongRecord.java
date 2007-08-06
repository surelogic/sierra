package com.surelogic.sierra.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A record that has a generate primary key with a long value may extend
 * LongRecord.
 * 
 * @author nathan
 * 
 */
public abstract class LongRecord implements Record<Long> {

	protected Long id;

	public Long getId() {
		return id;
	}

	public int fillWithPk(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, id);
		return idx;
	}

	public int readPk(ResultSet set, int idx) throws SQLException {
		this.id = set.getLong(idx++);
		return idx;
	}

	public void setId(Long id) {
		this.id = id;
	}

}

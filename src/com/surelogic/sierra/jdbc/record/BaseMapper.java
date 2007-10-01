package com.surelogic.sierra.jdbc.record;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseMapper implements RecordMapper {

	private final PreparedStatement insert;
	private final PreparedStatement select;
	private final PreparedStatement delete;

	public BaseMapper(Connection conn, String insertSql, String selectSql,
			String deleteSql) throws SQLException {
		if (insertSql != null) {
			this.insert = conn.prepareStatement(insertSql,
					Statement.RETURN_GENERATED_KEYS);
		} else {
			this.insert = null;
		}
		if (selectSql != null) {
			this.select = conn.prepareStatement(selectSql);
		} else {
			this.select = null;
		}
		if (deleteSql != null) {
			this.delete = conn.prepareStatement(deleteSql);
		} else {
			this.delete = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.surelogic.sierra.jdbc.RecordMapper#insert(com.surelogic.sierra.jdbc.AbstractRecord)
	 */
	public void insert(AbstractRecord<?> record) throws SQLException {
		if (insert == null)
			throw new UnsupportedOperationException();
		record.fill(insert, 1);
		insert.executeUpdate();
		ResultSet keys = insert.getGeneratedKeys();
		try {
			if (keys.next()) {
				record.readPk(keys, 1);
			}
		} finally {
			keys.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.surelogic.sierra.jdbc.RecordMapper#remove(com.surelogic.sierra.jdbc.AbstractRecord)
	 */
	public void remove(AbstractRecord<?> record) throws SQLException {
		if (delete == null)
			throw new UnsupportedOperationException();
		record.fillWithPk(delete, 1);
		delete.executeUpdate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.surelogic.sierra.jdbc.RecordMapper#select(com.surelogic.sierra.jdbc.AbstractRecord)
	 */
	public boolean select(AbstractRecord<?> record) throws SQLException {
		if (select == null)
			throw new UnsupportedOperationException();
		record.fillWithNk(select, 1);
		ResultSet set = select.executeQuery();
		try {
			boolean found = set.next();
			if (found) {
				record.readAttributes(set, record.readPk(set, 1));
			}
			return found;
		} finally {
			set.close();
		}

	}

}

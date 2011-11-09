package com.surelogic.sierra.jdbc.record;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.DBType;
import com.surelogic.common.jdbc.JDBCUtils;

public class BaseMapper implements RecordMapper {

	private static final String[] KEYS = new String[] { "ID" };

	private final PreparedStatement insert;
	private final PreparedStatement select;
	private final PreparedStatement delete;
	private final boolean hasKeys;

	public BaseMapper(Connection conn, String insertSql, String selectSql,
			String deleteSql, boolean generateKey) throws SQLException {
		this.hasKeys = generateKey;
		if (generateKey) {
			if (insertSql != null) {
				if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
					this.insert = conn.prepareStatement(insertSql, KEYS);
				} else {
					this.insert = conn.prepareStatement(insertSql,
							Statement.RETURN_GENERATED_KEYS);
				}
			} else {
				this.insert = null;
			}
		} else {
			if (insertSql != null) {
				this.insert = conn.prepareStatement(insertSql);
			} else {
				this.insert = null;
			}
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

	public BaseMapper(Connection conn, String insertSql, String selectSql,
			String deleteSql) throws SQLException {
		this(conn, insertSql, selectSql, deleteSql, true);
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
		if (hasKeys) {
			ResultSet keys = insert.getGeneratedKeys();
			try {
				if (keys.next()) {
					record.readPk(keys, 1);
				}
			} finally {
				keys.close();
			}
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

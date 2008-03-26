package com.surelogic.sierra.jdbc.qrecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;

public class BaseMapper implements RecordMapper {

	private static final String[] KEYS = new String[] { "ID" };

	private final PreparedStatement insert;
	private final PreparedStatement select;
	private final PreparedStatement delete;
	private final boolean hasKeys;

	public BaseMapper(Connection conn, String insertSql, String selectSql,
			String deleteSql, boolean generateKey) throws SQLException {
		hasKeys = generateKey;
		if (generateKey) {
			if (insertSql != null) {
				if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
					insert = conn.prepareStatement(insertSql, KEYS);
				} else {
					insert = conn.prepareStatement(insertSql,
							Statement.RETURN_GENERATED_KEYS);
				}
			} else {
				insert = null;
			}
		} else {
			if (insertSql != null) {
				insert = conn.prepareStatement(insertSql);
			} else {
				insert = null;
			}
		}
		if (selectSql != null) {
			select = conn.prepareStatement(selectSql);
		} else {
			select = null;
		}
		if (deleteSql != null) {
			delete = conn.prepareStatement(deleteSql);
		} else {
			delete = null;
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
	public void insert(AbstractRecord<?> record) {
		try {
			if (insert == null) {
				throw new UnsupportedOperationException();
			}
			record.fill(insert, 1);
			insert.executeUpdate();
			if (hasKeys) {
				final ResultSet keys = insert.getGeneratedKeys();
				try {
					if (keys.next()) {
						record.readPk(keys, 1);
					}
				} finally {
					keys.close();
				}
			}
		} catch (final SQLException e) {
			throw new RecordException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.surelogic.sierra.jdbc.RecordMapper#remove(com.surelogic.sierra.jdbc.AbstractRecord)
	 */
	public void remove(AbstractRecord<?> record) {
		if (delete == null) {
			throw new UnsupportedOperationException();
		}
		try {
			record.fillWithPk(delete, 1);
			delete.executeUpdate();
		} catch (final SQLException e) {
			throw new RecordException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.surelogic.sierra.jdbc.RecordMapper#select(com.surelogic.sierra.jdbc.AbstractRecord)
	 */
	public boolean select(AbstractRecord<?> record) {
		try {
			if (select == null) {
				throw new UnsupportedOperationException();
			}
			record.fillWithNk(select, 1);
			final ResultSet set = select.executeQuery();
			try {
				final boolean found = set.next();
				if (found) {
					record.readAttributes(set, record.readPk(set, 1));
				}
				return found;
			} finally {
				set.close();
			}
		} catch (final SQLException e) {
			throw new RecordException(e);
		}
	}

}

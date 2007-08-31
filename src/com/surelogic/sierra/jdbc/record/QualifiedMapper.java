package com.surelogic.sierra.jdbc.record;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
@Deprecated
public class QualifiedMapper implements RecordMapper {

	private final PreparedStatement insert;
	private final PreparedStatement select;
	private final PreparedStatement delete;
	protected final Long qualifier;

	public QualifiedMapper(Connection conn, String insertSql, String selectSql,
			String deleteSql, Long qualifier) throws SQLException {
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
		this.qualifier = qualifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.surelogic.sierra.jdbc.RecordMapper#insert(com.surelogic.sierra.jdbc.AbstractRecord)
	 */
	public void insert(AbstractRecord<?> record) throws SQLException {
		if (insert == null)
			throw new UnsupportedOperationException();
		ResultSet keys;
		insert.setLong(1, qualifier);
		record.fill(insert, 2);
		insert.executeUpdate();
		keys = insert.getGeneratedKeys();
		if (keys.next()) {// TODO this may not work
			record.readPk(keys, 1);
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
		delete.setLong(1, qualifier);
		record.fillWithPk(delete, 2);
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
		select.setLong(1, qualifier);
		record.fillWithNk(select, 2);
		ResultSet set = select.executeQuery();
		boolean found = set.next();
		if (found) {
			record.readAttributes(set, record.readPk(set, 1));
		}
		return found;
	}

}

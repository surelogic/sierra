package com.surelogic.sierra.jdbc.record;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateBaseMapper extends BaseMapper implements UpdateRecordMapper {

	private final PreparedStatement update;

	public UpdateBaseMapper(Connection conn, String insertSql,
			String selectSql, String deleteSql, String updateSql,
			boolean generateKeys) throws SQLException {
		super(conn, insertSql, selectSql, deleteSql, generateKeys);
		if (updateSql != null) {
			this.update = conn.prepareStatement(updateSql);
		} else {
			this.update = null;
		}
	}

	public UpdateBaseMapper(Connection conn, String insertSql,
			String selectSql, String deleteSql, String updateSql)
			throws SQLException {
		super(conn, insertSql, selectSql, deleteSql);
		if (updateSql != null) {
			this.update = conn.prepareStatement(updateSql);
		} else {
			this.update = null;
		}
	}

	public void update(UpdatableRecord<?> record) throws SQLException {
		if (update == null) {
			throw new UnsupportedOperationException();
		}
		int idx = record.fillUpdatedFields(update, 1);
		record.fillWithPk(update, idx);
		update.executeUpdate();
	}

}

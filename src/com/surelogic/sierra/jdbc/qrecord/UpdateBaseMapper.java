package com.surelogic.sierra.jdbc.qrecord;

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
			update = conn.prepareStatement(updateSql);
		} else {
			update = null;
		}
	}

	public UpdateBaseMapper(Connection conn, String insertSql,
			String selectSql, String deleteSql, String updateSql)
			throws SQLException {
		super(conn, insertSql, selectSql, deleteSql);
		if (updateSql != null) {
			update = conn.prepareStatement(updateSql);
		} else {
			update = null;
		}
	}

	public void update(AbstractUpdatableRecord<?> record) {
		try {
			if (update == null) {
				throw new UnsupportedOperationException();
			}
			final int idx = record.fillUpdatedFields(update, 1);
			record.fillWithPk(update, idx);
			update.executeUpdate();
		} catch (final SQLException e) {
			throw new RecordException(e);
		}
	}

}

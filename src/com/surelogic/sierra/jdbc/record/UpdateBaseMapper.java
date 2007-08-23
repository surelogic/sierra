package com.surelogic.sierra.jdbc.record;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateBaseMapper extends BaseMapper implements UpdateRecordMapper  {

	private final PreparedStatement update;

	public UpdateBaseMapper(Connection conn, String insertSql,
			String selectSql, String deleteSql, String updateSql)
			throws SQLException {
		super(conn, insertSql, selectSql, deleteSql);
		this.update = conn.prepareStatement(updateSql);
	}

	public void update(UpdatableRecord<?> record) throws SQLException {
		int idx = record.fillUpdatedFields(update, 1);
		record.fillWithPk(update, idx);
		update.executeUpdate();
	}

}

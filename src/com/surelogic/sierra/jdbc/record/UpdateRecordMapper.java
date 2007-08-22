package com.surelogic.sierra.jdbc.record;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateRecordMapper extends BaseMapper {

	private final PreparedStatement update;

	public UpdateRecordMapper(Connection conn, String insertSql,
			String selectSql, String deleteSql, String updateSql)
			throws SQLException {
		super(conn, insertSql, selectSql, deleteSql);
		this.update = conn.prepareStatement(updateSql);
	}

	public void update(UpdatableRecord<?> record) throws SQLException {
		int idx = record.fillWithAttributes(update, 1);
		record.fillWithPk(update, idx);
		update.executeUpdate();
	}

}

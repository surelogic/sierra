package com.surelogic.sierra.jdbc.record;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QualifiedUpdateRecordMapper extends QualifiedMapper implements UpdateRecordMapper {
	private final PreparedStatement update;

	public QualifiedUpdateRecordMapper(Connection conn, String insertSql,
			String selectSql, String deleteSql, String updateSql, Long qualifier)
			throws SQLException {
		super(conn, insertSql, selectSql, deleteSql, qualifier);
		this.update = conn.prepareStatement(updateSql);
	}

	/* (non-Javadoc)
	 * @see com.surelogic.sierra.jdbc.record.UpdateRecordMapper#update(com.surelogic.sierra.jdbc.record.UpdatableRecord)
	 */
	public void update(UpdatableRecord<?> record) throws SQLException {
		int idx = record.fillWithAttributes(update, 1);
		update.setLong(idx++, qualifier);
		record.fillWithPk(update, idx);
		update.executeUpdate();
	}
}

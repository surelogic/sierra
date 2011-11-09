package com.surelogic.sierra.jdbc.tool;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.FindingTypeRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public final class FindingTypeRecordFactory {

	private final UpdateRecordMapper findingTypeMapper;

	private FindingTypeRecordFactory(final Connection conn) throws SQLException {
		this.findingTypeMapper = new UpdateBaseMapper(
				conn,
				"INSERT INTO FINDING_TYPE (UUID,NAME,SHORT_MESSAGE,INFO) VALUES (?,?,?,?)",
				"SELECT ID,NAME,SHORT_MESSAGE,INFO FROM FINDING_TYPE WHERE UUID = ?",
				"DELETE FROM FINDING_TYPE WHERE ID = ?",
				"UPDATE FINDING_TYPE SET NAME = ?, SHORT_MESSAGE = ?, INFO = ? WHERE ID = ?");
	}

	public FindingTypeRecord newFindingTypeRecord() {
		return new FindingTypeRecord(findingTypeMapper);
	}

	public static FindingTypeRecordFactory getInstance(final Connection conn)
			throws SQLException {
		return new FindingTypeRecordFactory(conn);
	}
}

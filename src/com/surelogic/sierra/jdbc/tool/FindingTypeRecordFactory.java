package com.surelogic.sierra.jdbc.tool;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.CategoryRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public class FindingTypeRecordFactory {

	private final UpdateRecordMapper findingTypeMapper;
	private final UpdateRecordMapper categoryMapper;

	private FindingTypeRecordFactory(Connection conn) throws SQLException {
		this.findingTypeMapper = new UpdateBaseMapper(
				conn,
				"INSERT INTO FINDING_TYPE (UID,NAME,SHORT_MESSAGE,INFO) VALUES (?,?,?,?)",
				"SELECT ID,NAME,SHORT_MESSAGE,INFO FROM FINDING_TYPE WHERE UID = ?",
				"DELETE FROM FINDING_TYPE WHERE ID = ?",
				"UPDATE FINDING_TYPE SET NAME = ?, SHORT_MESSAGE = ?, INFO = ? WHERE ID = ?");
		this.categoryMapper = new UpdateBaseMapper(
				conn,
				"INSERT INTO FINDING_CATEGORY (UID,NAME,DESCRIPTION) VALUES (?,?,?)",
				"SELECT ID,NAME,DESCRIPTION FROM FINDING_CATEGORY WHERE UID = ?",
				"DELETE FROM FINDING_CATEGORY WHERE ID = ?",
				"UPDATE FINDING_CATEGORY SET NAME = ?, DESCRIPTION = ? WHERE ID = ?");
	}

	public FindingTypeRecord newFindingTypeRecord() {
		return new FindingTypeRecord(findingTypeMapper);
	}

	public CategoryRecord newCategoryRecord() {
		return new CategoryRecord(categoryMapper);
	}

	public static FindingTypeRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new FindingTypeRecordFactory(conn);
	}
}

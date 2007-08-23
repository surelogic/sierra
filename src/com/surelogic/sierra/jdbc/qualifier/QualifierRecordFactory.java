package com.surelogic.sierra.jdbc.qualifier;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.QualifierRecord;

public class QualifierRecordFactory {

	private static final String QUALIFIER_SELECT = "SELECT ID FROM QUALIFIER WHERE NAME = ?";
	private static final String QUALIFIER_INSERT = "INSERT INTO QUALIFIER (NAME) VALUES (?)";
	private static final String QUALIFIER_DELETE = "DELETE FROM QUALIFIER WHERE ID = ?";

	private final Connection conn;

	private BaseMapper qualifierMapper;

	private QualifierRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;
	}

	public static QualifierRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new QualifierRecordFactory(conn);
	}

	QualifierRecord newQualifier() throws SQLException {
		if (qualifierMapper == null) {
			qualifierMapper = new BaseMapper(conn, QUALIFIER_INSERT,
					QUALIFIER_SELECT, QUALIFIER_DELETE);
		}
		return new QualifierRecord(qualifierMapper);
	}

}

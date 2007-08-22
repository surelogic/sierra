package com.surelogic.sierra.jdbc.qualifier;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.RunQualifierRecord;

public class QualifierRecordFactory {
	private static final String QUALIFIER_SELECT = "SELECT ID FROM QUALIFIER WHERE NAME = ?";
	private static final String RUN_QUALIFIER_INSERT = "INSERT INTO QUALIFIER_RUN_RELTN (QUALIFIER_ID,RUN_ID) VALUES(?,?)";

	private final Connection conn;
	
	private BaseMapper qualifierMapper;
	private BaseMapper runQualMapper;
	
	private QualifierRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;
	}

	public static QualifierRecordFactory getInstance(Connection conn) throws SQLException {
		return new QualifierRecordFactory(conn);
	}
	
	QualifierRecord newQualifier() throws SQLException {
		if (qualifierMapper == null) {
			qualifierMapper = new BaseMapper(conn, null, QUALIFIER_SELECT, null);
		}
		return new QualifierRecord(qualifierMapper);
	}

	RunQualifierRecord newRunQualiferRelation() throws SQLException {
		if (runQualMapper == null) {
			runQualMapper = new BaseMapper(conn, RUN_QUALIFIER_INSERT, null,
					null);
		}
		return new RunQualifierRecord(runQualMapper);
	}
}

package com.surelogic.sierra.jdbc.qualifier;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;

public class QualifierRecordFactory {

	private static final String QUALIFIER_SELECT = "SELECT ID FROM QUALIFIER WHERE NAME = ?";
	private static final String QUALIFIER_INSERT = "INSERT INTO QUALIFIER (NAME) VALUES (?)";
	private static final String QUALIFIER_DELETE = "DELETE FROM QUALIFIER WHERE ID = ?";
	private static final String QUALIFIER_UPDATE = "UPDATE QUALIFIER SET NAME = ? WHERE ID = ?";

	@SuppressWarnings("unused")
	private final Connection conn;

	private UpdateBaseMapper qualifierMapper;

	private QualifierRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;
		
		qualifierMapper = new UpdateBaseMapper(conn, QUALIFIER_INSERT,
				QUALIFIER_SELECT, QUALIFIER_DELETE, QUALIFIER_UPDATE);
	}

	public static QualifierRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new QualifierRecordFactory(conn);
	}
	
	public QualifierRecord newQualifier() throws SQLException {
		return new QualifierRecord(qualifierMapper);
	}

}

package com.surelogic.sierra.jdbc.timeseries;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.TimeseriesRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;

public final class TimeseriesRecordFactory {

	private static final String QUALIFIER_SELECT = "SELECT ID FROM QUALIFIER WHERE NAME = ?";
	private static final String QUALIFIER_INSERT = "INSERT INTO QUALIFIER (NAME) VALUES (?)";
	private static final String QUALIFIER_DELETE = "DELETE FROM QUALIFIER WHERE ID = ?";
	private static final String QUALIFIER_UPDATE = "UPDATE QUALIFIER SET NAME = ? WHERE ID = ?";

	@SuppressWarnings("unused")
	private final Connection conn;

	private final UpdateBaseMapper qualifierMapper;

	private TimeseriesRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;
		
		qualifierMapper = new UpdateBaseMapper(conn, QUALIFIER_INSERT,
				QUALIFIER_SELECT, QUALIFIER_DELETE, QUALIFIER_UPDATE);
	}

	public static TimeseriesRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new TimeseriesRecordFactory(conn);
	}
	
	public TimeseriesRecord newQualifier() throws SQLException {
		return new TimeseriesRecord(qualifierMapper);
	}

}

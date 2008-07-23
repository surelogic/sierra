package com.surelogic.sierra.jdbc.timeseries;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.TimeseriesRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;

public final class TimeseriesRecordFactory {

	private static final String TIMESERIES_SELECT = "SELECT ID FROM QUALIFIER WHERE NAME = ?";
	private static final String TIMESERIES_INSERT = "INSERT INTO QUALIFIER (NAME) VALUES (?)";
	private static final String TIMESERIES_DELETE = "DELETE FROM QUALIFIER WHERE ID = ?";
	private static final String TIMESERIES_UPDATE = "UPDATE QUALIFIER SET NAME = ? WHERE ID = ?";

	@SuppressWarnings("unused")
	private final Connection conn;

	private final UpdateBaseMapper timeseriesMapper;

	private TimeseriesRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;
		
		timeseriesMapper = new UpdateBaseMapper(conn, TIMESERIES_INSERT,
				TIMESERIES_SELECT, TIMESERIES_DELETE, TIMESERIES_UPDATE);
	}

	public static TimeseriesRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new TimeseriesRecordFactory(conn);
	}
	
	public TimeseriesRecord newTimeseries() throws SQLException {
		return new TimeseriesRecord(timeseriesMapper);
	}

}

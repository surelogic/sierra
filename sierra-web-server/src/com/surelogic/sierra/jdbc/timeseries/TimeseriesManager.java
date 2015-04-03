package com.surelogic.sierra.jdbc.timeseries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.jdbc.record.TimeseriesRecord;

public final class TimeseriesManager {

	public static final String ALL_SCANS = "__ALL_SCANS__";

	@SuppressWarnings("unused")
	private final Connection conn;

	private static final String FIND_ALL = "SELECT NAME FROM QUALIFIER";
	private final PreparedStatement findAllTimeseriesNames;

	private final TimeseriesRecordFactory timeseriesFactory;

	private TimeseriesManager(Connection conn) throws SQLException {
		this.conn = conn;

		timeseriesFactory = TimeseriesRecordFactory.getInstance(conn);

		findAllTimeseriesNames = conn.prepareStatement(FIND_ALL);
	}

	public List<String> getAllTimeseriesNames() throws SQLException {
		ResultSet rs = findAllTimeseriesNames.executeQuery();
		List<String> timeseriesNames = new ArrayList<String>();
		try {
			while (rs.next()) {
				final String name = rs.getString(1);
				if (!ALL_SCANS.equals(name)) {
					timeseriesNames.add(name);
				}
			}
		} finally {
			rs.close();
		}
		return timeseriesNames;
	}

	public void deleteTimeseries(String name) throws SQLException {
		final TimeseriesRecord timeseries = timeseriesFactory.newTimeseries();
		timeseries.setName(name);
		if (ALL_SCANS.equals(name) || !timeseries.select()) {
			throw new IllegalArgumentException("Timeseries with the name "
					+ name + " does not exist or could not be deleted.");
		}
		timeseries.delete();
	}

	public long newTimeseries(String name) throws SQLException {
		final TimeseriesRecord timeseries = timeseriesFactory.newTimeseries();
		timeseries.setName(name);
		/** If this timeseries already exists, throw an error */
		if (ALL_SCANS.equals(name) || timeseries.select()) {
			throw new IllegalArgumentException(
					"Timeseries with this name already exists");
		}
		timeseries.insert();
		return timeseries.getId();
	}

	public void renameTimeseries(String currName, String newName)
			throws SQLException {
		final TimeseriesRecord timeseries = timeseriesFactory.newTimeseries();
		timeseries.setName(currName);

		/** If this timeseries does not exist, throw an error */
		if (ALL_SCANS.equals(currName) || ALL_SCANS.equals(newName)
				|| !timeseries.select()) {
			throw new IllegalArgumentException("Timeseries with the name "
					+ currName + " does not exist or could not be renamed to "
					+ newName + ".");
		}
		timeseries.setName(newName);
		timeseries.update();
	}

	public static TimeseriesManager getInstance(Connection conn)
			throws SQLException {
		return new TimeseriesManager(conn);
	}
}

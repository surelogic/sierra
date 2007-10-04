package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.jdbc.qualifier.QualifierRecordFactory;
import com.surelogic.sierra.jdbc.record.QualifierRecord;

/**
 * Builds queries for accessing time series reporting data.
 * 
 * @author nathan
 * 
 */
public class TimeSeriesQueryBuilder {

	private final Connection conn;

	private final PreparedStatement getLatestScansByQualifierName;

	private Long qualifierId;
	private final List<Long> scanIds;
	private final StringBuilder builder;

	private TimeSeriesQueryBuilder(Connection conn) {
		this.scanIds = new ArrayList<Long>();
		this.builder = new StringBuilder();
		this.conn = conn;
		try {
			this.getLatestScansByQualifierName = conn
					.prepareStatement("SELECT SCAN_ID FROM LATEST_SCANS ");
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	public static TimeSeriesQueryBuilder getInstance(Connection conn)
			throws SQLException {
		return new TimeSeriesQueryBuilder(conn);
	}

	public String queryProjectRelevantOrIrrelevantCounts(String project) {
		builder.setLength(0);
		
		return builder.toString();
	}
	
	public String queryAllImportanceCounts() {
		builder.setLength(0);
		builder
				.append("SELECT TSO.IMPORTANCE, COUNT(TSO.FINDING_ID) FROM SCAN_OVERVIEW SO, TIME_SERIES_OVERVIEW TSO WHERE SO.SCAN_ID IN ");
		inClause(builder, scanIds);
		builder
				.append(" AND TSO.FINDING_ID = SO.FINDING_ID GROUP BY TSO.IMPORTANCE");
		return builder.toString();
	}

	public String queryAllRelevantOrIrrelevantCounts() {
		builder.setLength(0);
		builder.append("SELECT * FROM");
		builder
				.append("(SELECT COUNT(SO.FINDING_ID) FROM SCAN_OVERVIEW SO, TIME_SERIES_OVERVIEW TSO WHERE SO.SCAN_ID IN ");
		inClause(builder, scanIds);
		builder
				.append(" AND TSO.FINDING_ID = SO.FINDING_ID AND TSO.IMPORTANCE='Irrelevant') AS IRRELEVANT");
		builder.append(",");
		builder
				.append("(SELECT COUNT(SO.FINDING_ID) FROM SCAN_OVERVIEW SO, TIME_SERIES_OVERVIEW TSO WHERE SO.SCAN_ID IN ");
		inClause(builder, scanIds);
		builder
				.append(" AND TSO.FINDING_ID = SO.FINDING_ID AND TSO.IMPORTANCE!='Irrelevant') AS RELEVANT");
		return builder.toString();
	}
	
	public String queryAllFindingTypeCounts() {
		builder.setLength(0);
		builder
				.append("SELECT TSO.FINDING_TYPE, COUNT(TSO.FINDING_ID) FROM SCAN_OVERVIEW SO, TIME_SERIES_OVERVIEW TSO WHERE SO.SCAN_ID IN ");
		inClause(builder, scanIds);
		builder
				.append(" AND TSO.FINDING_ID = SO.FINDING_ID GROUP BY TSO.FINDING_TYPE");
		return builder.toString();
	}

	public void setContext(String timeSeries) {
		QualifierRecord q;
		try {
			q = QualifierRecordFactory.getInstance(conn).newQualifier();
			if (q.select()) {
				this.qualifierId = q.getId();
				scanIds.clear();
				getLatestScansByQualifierName.setString(1, timeSeries);
				ResultSet set = getLatestScansByQualifierName.executeQuery();
				try {
					while (set.next()) {
						scanIds.add(set.getLong(1));
					}
				} finally {
					set.close();
				}
			} else {
				throw new IllegalArgumentException(timeSeries
						+ " is not a valid name for a time series/qualifier.");
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}

	}

	private static void inClause(StringBuilder builder, List<?> values) {
		builder.append("(");
		for (Object o : values) {
			builder.append(o);
			builder.append(", ");
		}
		builder.append(")");
	}
}

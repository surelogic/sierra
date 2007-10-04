package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.util.List;

public class ProjectSeriesQueryBuilder {

	private final Connection conn;
	private List<Long> scanIds;

	private ProjectSeriesQueryBuilder(Connection conn) {
		this.conn = conn;
	}

	public void setContext(String project, String timeSeries) {

	}

	public static ProjectSeriesQueryBuilder getInstance(Connection conn) {
		return new ProjectSeriesQueryBuilder(conn);
	}
}

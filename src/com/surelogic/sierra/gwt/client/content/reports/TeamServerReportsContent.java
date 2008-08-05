package com.surelogic.sierra.gwt.client.content.reports;

import com.surelogic.sierra.gwt.client.data.Report.DataSource;

public class TeamServerReportsContent extends ReportsContent {
	private static final TeamServerReportsContent instance = new TeamServerReportsContent();

	public static TeamServerReportsContent getInstance() {
		return instance;
	}

	private TeamServerReportsContent() {
		super();
		// singleton
	}

	@Override
	protected DataSource getDataSource() {
		return DataSource.TEAMSERVER;
	}

}

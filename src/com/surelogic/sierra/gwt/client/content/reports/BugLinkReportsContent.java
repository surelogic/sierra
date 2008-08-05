package com.surelogic.sierra.gwt.client.content.reports;

import com.surelogic.sierra.gwt.client.data.Report.DataSource;

public class BugLinkReportsContent extends ReportsContent {
	private static final BugLinkReportsContent instance = new BugLinkReportsContent();

	public static BugLinkReportsContent getInstance() {
		return instance;
	}

	private BugLinkReportsContent() {
		super();
		// singleton
	}

	@Override
	protected DataSource getDataSource() {
		return DataSource.BUGLINK;
	}

}

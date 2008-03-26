package com.surelogic.sierra.gwt.client;

public class UserHeader extends AuthenticatedHeader {
	private static final UserHeader instance = new UserHeader();

	public static UserHeader getInstance() {
		return instance;
	}

	protected void addTabs() {
		addTab("Welcome", OverviewContent.getInstance());
		addTab("Filter Set", FilterSetContent.getInstance());
		addTab("Finding", FindingContent.getInstance());
	}
}

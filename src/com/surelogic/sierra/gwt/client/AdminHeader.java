package com.surelogic.sierra.gwt.client;

public class AdminHeader extends AuthenticatedHeader {
	private static final AdminHeader instance = new AdminHeader();

	public static AdminHeader getInstance() {
		return instance;
	}

	protected void addTabs() {
		addTab("Settings", SettingsContent.getInstance());
		addTab("Users", UserManagementContent.getInstance());
	}
}

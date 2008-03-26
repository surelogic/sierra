package com.surelogic.sierra.gwt.client;

public class AdminHeader extends AuthenticatedHeader {
	private static final AdminHeader instance = new AdminHeader();

	public static AdminHeader getInstance() {
		return instance;
	}

	private AdminHeader() {
		super();
	}

	protected void addUtilities() {
		// do nothing for now
	}

	protected void addTabs() {
		addTab("Settings", SettingsContent.getInstance());
		addTab("Users", UserManagementContent.getInstance());
	}
}

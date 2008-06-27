package com.surelogic.sierra.gwt.client.header;

import com.surelogic.sierra.gwt.client.content.servermgmt.ServerManagementContent;
import com.surelogic.sierra.gwt.client.content.settings.SettingsContent;
import com.surelogic.sierra.gwt.client.content.usermgmt.UserManagementContent;

public final class AdminHeader extends AuthenticatedHeader {
	private static final AdminHeader instance = new AdminHeader();

	public static AdminHeader getInstance() {
		return instance;
	}

	private AdminHeader() {
		super();
	}

	@Override
	protected void addUtilities() {
		// do nothing for now
	}

	@Override
	protected void addTabs() {
		addTab("Settings", "admin", SettingsContent.getInstance());
		addTab("Users", "admin", UserManagementContent.getInstance());
		addTab("Servers", "admin", ServerManagementContent.getInstance());
	}
}

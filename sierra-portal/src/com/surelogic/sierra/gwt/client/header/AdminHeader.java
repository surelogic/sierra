package com.surelogic.sierra.gwt.client.header;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.content.overview.OverviewContent;
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
		addUtilityItem("Return To Site", new ClickListener() {

			public void onClick(final Widget sender) {
				OverviewContent.getInstance().show();
			}
		});

		addUtilitySeparator();
	}

	@Override
	protected void addTabs() {
		addTab(SettingsContent.getInstance(), "admin");
		addTab(UserManagementContent.getInstance(), "admin");
		addTab(ServerManagementContent.getInstance(), "admin");
	}
}

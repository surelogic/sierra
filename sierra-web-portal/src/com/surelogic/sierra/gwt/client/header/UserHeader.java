package com.surelogic.sierra.gwt.client.header;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.content.categories.CategoriesContent;
import com.surelogic.sierra.gwt.client.content.extensions.ExtensionsContent;
import com.surelogic.sierra.gwt.client.content.finding.FindingContent;
import com.surelogic.sierra.gwt.client.content.findingtypes.FindingTypesContent;
import com.surelogic.sierra.gwt.client.content.overview.OverviewContent;
import com.surelogic.sierra.gwt.client.content.projects.ProjectsContent;
import com.surelogic.sierra.gwt.client.content.reports.TeamServerReportsContent;
import com.surelogic.sierra.gwt.client.content.scanfilters.ScanFiltersContent;
import com.surelogic.sierra.gwt.client.content.scans.ScanContent;
import com.surelogic.sierra.gwt.client.content.settings.SettingsContent;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public final class UserHeader extends AuthenticatedHeader {
	private static final UserHeader instance = new UserHeader();
	private Label manageSite;
	private Label manageSiteSeparator;

	public static UserHeader getInstance() {
		return instance;
	}

	private UserHeader() {
		super();
	}

	@Override
	protected void addUtilities() {
		manageSite = addUtilityItem("Manage Site", new ClickListener() {

			public void onClick(final Widget sender) {
				SettingsContent.getInstance().show();
			}
		});
		manageSiteSeparator = addUtilitySeparator();
	}

	@Override
	protected void addTabs() {
		addTab(OverviewContent.getInstance(), "welcome");
		addTabSpacer();
		addTab(FindingTypesContent.getInstance(), "buglink");
		addTab(CategoriesContent.getInstance(), "buglink");
		addTab(ScanFiltersContent.getInstance(), "buglink");
		addTab(ExtensionsContent.getInstance(), "buglink");
		addTabSpacer();
		addTab(ProjectsContent.getInstance(), "team");
		addTab(ScanContent.getInstance(), "team");
		addTab(FindingContent.getInstance(), "team");
		addTab(TeamServerReportsContent.getInstance(), "team");
	}

	@Override
	protected void onUpdateUser(final UserAccount user) {
		super.onUpdateUser(user);

		if (user != null) {
			if (user.isAdministrator()) {
				manageSite.setVisible(true);
				manageSiteSeparator.setVisible(true);
			} else {
				manageSite.setVisible(false);
				manageSiteSeparator.setVisible(false);
			}
		}
	}
}

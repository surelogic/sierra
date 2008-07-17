package com.surelogic.sierra.gwt.client.header;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.content.ContentRegistry;
import com.surelogic.sierra.gwt.client.content.categories.CategoriesContent;
import com.surelogic.sierra.gwt.client.content.finding.FindingContent;
import com.surelogic.sierra.gwt.client.content.findingtypes.FindingTypesContent;
import com.surelogic.sierra.gwt.client.content.overview.OverviewContent;
import com.surelogic.sierra.gwt.client.content.projects.ProjectsContent;
import com.surelogic.sierra.gwt.client.content.reports.ReportsContent;
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
				final String url = ContentRegistry
						.getContentUrl(SettingsContent.getInstance());
				Window
						.open(url.toString(), null,
								"menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes");
			}
		});
		manageSiteSeparator = addUtilitySeparator();
	}

	@Override
	protected void addTabs() {
		addTab(OverviewContent.getInstance(), "welcome");
		addTabSpacer();
		addTab(CategoriesContent.getInstance(), "buglink");
		addTab(FindingTypesContent.getInstance(), "buglink");
		addTab(ScanFiltersContent.getInstance(), "buglink");
		addTabSpacer();
		addTab(ProjectsContent.getInstance(), "team");
		addTab(ScanContent.getInstance(), "team");
		addTab(FindingContent.getInstance(), "team");
		addTabSpacer();
		addTab(ReportsContent.getInstance(), "reports");
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

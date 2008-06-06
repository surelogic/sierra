package com.surelogic.sierra.gwt.client.header;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ContentRegistry;
import com.surelogic.sierra.gwt.client.FindingContent;
import com.surelogic.sierra.gwt.client.OverviewContent;
import com.surelogic.sierra.gwt.client.SettingsContent;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.reports.ReportsContent;
import com.surelogic.sierra.gwt.client.rules.CategoriesContent;
import com.surelogic.sierra.gwt.client.rules.FindingTypesContent;
import com.surelogic.sierra.gwt.client.rules.ScanFiltersContent;

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

			public void onClick(Widget sender) {
				final String url = ContentRegistry
						.getContentUrl(SettingsContent.getInstance());
				Window.open(url.toString(), null, null);
			}
		});
		manageSiteSeparator = addUtilitySeparator();
	}

	@Override
	protected void addTabs() {
		addTab("Welcome", OverviewContent.getInstance());
		addTab("Categories", CategoriesContent.getInstance());
		addTab("Finding Types", FindingTypesContent.getInstance());
		addTab("Scan Filters", ScanFiltersContent.getInstance());
		addTab("Finding", FindingContent.getInstance());
		addTab("Reports", ReportsContent.getInstance());
	}

	@Override
	protected void onUpdateUser(UserAccount user) {
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

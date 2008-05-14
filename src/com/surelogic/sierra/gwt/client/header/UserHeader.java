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
import com.surelogic.sierra.gwt.client.rules.CategoriesContent;
import com.surelogic.sierra.gwt.client.rules.FilterSetContent;
import com.surelogic.sierra.gwt.client.rules.FindingTypeContent;
import com.surelogic.sierra.gwt.client.rules.ScanFilterContent;

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

	protected void addTabs() {
		addTab("Welcome", OverviewContent.getInstance());
		addTab("Categories", CategoriesContent.getInstance());
		addTab("Filter Set", FilterSetContent.getInstance());
		addTab("Scan Filters", ScanFilterContent.getInstance());
		addTab("Finding", FindingContent.getInstance());
		addTab("Finding Type", FindingTypeContent.getInstance());
	}

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

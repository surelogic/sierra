package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public class UserHeader extends HeaderComposite {
	private static final UserHeader instance = new UserHeader();

	private Label userName;
	private Label manageSite;
	private Label manageSiteSeparator;

	public static UserHeader getInstance() {
		return instance;
	}

	public String getName() {
		return "user";
	}

	protected void onInitialize(VerticalPanel rootPanel) {
		StringBuffer location = new StringBuffer();
		location.append("Logged in to ");
		location.append(GWT.getHostPageBaseURL().replaceFirst(".*//", "")
				.replaceFirst("/sl.*", ""));
		location.append(" as");
		addUtilityItem(location.toString(), null);

		userName = addUtilityItem(null, null);
		userName.addStyleName("user");
		addUtilitySeparator();

		manageSite = addUtilityItem("Manage Site", new ClickListener() {

			public void onClick(Widget sender) {
				String url = ContentRegistry.getContentUrl(SettingsContent
						.getInstance());
				Window.open(url.toString(), null, null);
			}
		});
		manageSiteSeparator = addUtilitySeparator();

		addUtilityItem("Log out", new ClickListener() {

			public void onClick(Widget sender) {
				ClientContext.logout(null);
			}
		});

		addTab("Welcome", OverviewContent.getInstance());
		addTab("Users", UserManagementContent.getInstance());
		addTab("Filter Set", FilterSetContent.getInstance());
		addTab("Finding", FindingContent.getInstance());
	}

	protected void onActivate(Context context, UserAccount user) {
		// TODO Auto-generated method stub
	}

	protected void onUpdateUser(UserAccount user) {
		if (user != null) {
			userName.setText(user.getUserName());

			if (user.isAdministrator()) {
				manageSite.setVisible(true);
				manageSiteSeparator.setVisible(true);
			} else {
				manageSite.setVisible(false);
				manageSiteSeparator.setVisible(false);
			}
		}
	}

	protected void onUpdateContext(Context context) {
		// TODO Auto-generated method stub
	}

	protected void onDeactivate() {
		// TODO Auto-generated method stub

	}

}

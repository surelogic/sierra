package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public abstract class AuthenticatedHeader extends HeaderComposite {
	private Label userName;

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

		addUtilities();

		addUtilityItem("Log out", new ClickListener() {

			public void onClick(Widget sender) {
				ClientContext.logout(null);
			}
		});

		addTabs();
	}

	protected abstract void addUtilities();

	protected abstract void addTabs();

	protected void onActivate(Context context, UserAccount user) {
		// nothing to do
	}

	protected void onDeactivate() {
		// nothing to do
	}

	protected void onUpdateContext(Context context) {
		// nothing to do
	}

	protected void onUpdateUser(UserAccount user) {
		if (user != null) {
			userName.setText(user.getUserName());
		}
	}
}

package com.surelogic.sierra.gwt.client.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.SessionManager;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public abstract class AuthenticatedHeader extends HeaderComposite {
	private Label userName;

	@Override
	protected void onInitialize(final VerticalPanel rootPanel) {
		final StringBuffer location = new StringBuffer();
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

			public void onClick(final Widget sender) {
				SessionManager.logout(null);
			}
		});

		addTabs();
	}

	protected abstract void addUtilities();

	protected abstract void addTabs();

	@Override
	protected void onActivate(final Context context, final UserAccount user) {
		// nothing to do
	}

	@Override
	protected void onDeactivate() {
		// nothing to do
	}

	@Override
	protected void onUpdateContext(final Context context) {
		// nothing to do
	}

	@Override
	protected void onUpdateUser(final UserAccount user) {
		if (user != null) {
			userName.setText(user.getUserName());
		}
	}
}

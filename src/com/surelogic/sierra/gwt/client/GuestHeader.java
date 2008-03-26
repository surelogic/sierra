package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public class GuestHeader extends HeaderComposite {
	private static final GuestHeader instance = new GuestHeader();

	public static GuestHeader getInstance() {
		return instance;
	}

	protected void onInitialize(VerticalPanel rootPanel) {
		// nothing to do
	}

	protected void onActivate(Context context, UserAccount user) {
		// nothing to do
	}

	protected void onUpdateContext(Context context) {
		// nothing to do
	}

	protected void onUpdateUser(UserAccount user) {
		// nothing to do
	}

	protected void onDeactivate() {
		// nothing to do
	}
}

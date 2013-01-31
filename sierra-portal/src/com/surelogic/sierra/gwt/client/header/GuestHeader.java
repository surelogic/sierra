package com.surelogic.sierra.gwt.client.header;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public class GuestHeader extends HeaderComposite {
	private static final GuestHeader instance = new GuestHeader();

	public static GuestHeader getInstance() {
		return instance;
	}

	@Override
  protected void onInitialize(VerticalPanel rootPanel) {
		// nothing to do
	}

	@Override
  protected void onActivate(Context context, UserAccount user) {
		// nothing to do
	}

	@Override
  protected void onUpdateContext(Context context) {
		// nothing to do
	}

	@Override
  protected void onUpdateUser(UserAccount user) {
		// nothing to do
	}

	@Override
  protected void onDeactivate() {
		// nothing to do
	}
}

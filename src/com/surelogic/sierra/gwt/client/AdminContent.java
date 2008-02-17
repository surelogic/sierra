package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.TabPanel;

public class AdminContent extends ContentComposite {
	private static final AdminContent instance = new AdminContent();

	private final TabPanel tp = new TabPanel();

	public static AdminContent getInstance() {
		return instance;
	}

	private AdminContent() {
		super();
	}

	protected void onInitialize(DockPanel rootPanel) {
		tp.addStyleName(rootPanel.getStyleName());
		rootPanel.add(tp, DockPanel.CENTER);
	}

	protected void onActivate() {
		// FIXME don't actually rebuild the UI, separate and refresh the data
		// ManageServerPane and ManageUserAdminPane creation do service calls so
		// it has to be this way until fixed

		final AdminServerTab adminServer = new AdminServerTab();
		tp.add(adminServer, adminServer.getName());
		final AdminUsersTab adminUsers = new AdminUsersTab();
		tp.add(adminUsers, adminUsers.getName());
		final AdminUsers2Tab adminUsers2 = new AdminUsers2Tab();
		tp.add(adminUsers2, adminUsers2.getName());
		tp.selectTab(2);
	}

	protected boolean onDeactivate() {
		tp.clear();
		return true;
	}

}

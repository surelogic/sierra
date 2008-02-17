package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.TabPanel;

public class AdminPanel extends ContentComposite {

	public AdminPanel() {
		super();
	}

	public void activate() {
		final DockPanel rootPanel = getRootPanel();
		// FIXME don't actually rebuild the UI, separate and refresh the data
		// ManageServerPane and ManageUserAdminPane creation do service calls so
		// it has to be this way until fixed
		rootPanel.clear();

		final TabPanel tp = new TabPanel();
		tp.addStyleName(rootPanel.getStyleName());
		final AdminServerTab adminServer = new AdminServerTab();
		tp.add(adminServer, adminServer.getName());
		final AdminUsersTab adminUsers = new AdminUsersTab();
		tp.add(adminUsers, adminUsers.getName());
		final AdminUsers2Tab adminUsers2 = new AdminUsers2Tab();
		tp.add(adminUsers2, adminUsers2.getName());
		tp.selectTab(2);
		rootPanel.add(tp, DockPanel.CENTER);
	}

}

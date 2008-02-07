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
		tp.add(new ManageServerPane(), "Manage Server");
		tp.add(new ManageUserAdminPane(), "Manage Users");
		tp.selectTab(0);
		rootPanel.add(tp, DockPanel.CENTER);
	}

}

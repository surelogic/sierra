package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SierraPortal implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		TabPanel panel = new TabPanel();
		panel.add(new ManageServerPane(), "Server");
		panel.add(new ManageUserAdminPane(), "User Admin");
		RootPanel.get("content-pane").add(panel);
	}

}

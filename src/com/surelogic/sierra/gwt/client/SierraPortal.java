package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SierraPortal implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		VerticalPanel paneHolder = new VerticalPanel();
		paneHolder.add(new ManageServerPane());
		paneHolder.add(new ManageUserAdminPane());
		RootPanel.get("content-pane").add(paneHolder);
	}

}

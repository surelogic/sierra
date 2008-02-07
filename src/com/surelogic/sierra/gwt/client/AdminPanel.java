package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

public class AdminPanel extends Composite {
	private final DockPanel rootPanel = new DockPanel();
	
	public AdminPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");
		rootPanel.setHeight("100%");
		
		// TODO temp for admin stuff
		final Button activateButton = new Button("Load Admin UI");
		activateButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				final TabPanel tp = new TabPanel();
				tp.add(new ManageServerPane(), "Manage Server");
				tp.add(new ManageUserAdminPane(), "Manage Users");
				rootPanel.remove(activateButton);
				rootPanel.add(tp, DockPanel.CENTER);
				activateButton.setEnabled(false);
			}
		});
		rootPanel.add(activateButton, DockPanel.CENTER);
	}
	
	// TabPanel panel = new TabPanel();
// panel.add(new ManageServerPane(), "Server");
// panel.add(new ManageUserAdminPane(), "User Admin");
// RootPanel.get("content-pane").add(panel);
}

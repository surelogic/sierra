package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class HeaderPanel extends Composite {
	private final DockPanel rootPanel = new DockPanel();
	private final HorizontalPanel userPanel = new HorizontalPanel();

	public static HeaderPanel getInstance() {
		return (HeaderPanel) RootPanel.get("header-pane").getWidget(0);
	}

	public HeaderPanel() {
		super();
		initWidget(rootPanel);

		rootPanel.setVerticalAlignment(DockPanel.ALIGN_MIDDLE);
		rootPanel.setWidth("100%");

		Image sierraLogo = new Image(GWT.getModuleBaseURL()
				+ "images/header-sierra-logo.gif");
		rootPanel.add(sierraLogo, DockPanel.WEST);

		rootPanel.add(userPanel, DockPanel.EAST);
	}

	public void updateAccountPanel(UserAccount user) {
		if (user != null) {
			userPanel.add(new Label("Logged In: " + user.getUserName()));
			// TODO show preferences, logout, admin, etc (user panel)
		} else {
			userPanel.clear();
			// TODO hide user panel
		}
	}
}

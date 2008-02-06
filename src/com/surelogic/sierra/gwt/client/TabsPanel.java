package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

public class TabsPanel extends Composite {
	private final DockPanel rootPanel = new DockPanel();
	private final TabBar tabBar = new TabBar();

	public TabsPanel() {
		super();
		initWidget(rootPanel);

		rootPanel.setWidth("100%");
		rootPanel.setHeight("100%");

		rootPanel.add(tabBar, DockPanel.NORTH);
		tabBar.addTab("Home");

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

	public void selectTab(String tabName) {
		if (tabName == null) {
			tabBar.selectTab(0);
		} else {
			for (int i = 0; i < tabBar.getTabCount(); i++) {
				if (tabBar.getTabHTML(i).equals(tabName)) {
					if (i != tabBar.getSelectedTab()) {
						tabBar.selectTab(i);
						break;
					}
				}
			}
		}
	}

}

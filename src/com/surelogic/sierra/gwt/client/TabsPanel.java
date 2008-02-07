package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.TabBar;

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

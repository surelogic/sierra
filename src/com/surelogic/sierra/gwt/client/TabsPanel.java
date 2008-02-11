package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.TabBar;

public class TabsPanel extends ContentComposite {
	private final TabBar tabBar = new TabBar();

	public TabsPanel() {
		super();
		tabBar.addTab("Home");

		final DockPanel rootPanel = getRootPanel();
		rootPanel.add(tabBar, DockPanel.NORTH);
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

	public void activate() {
		if (tabBar.getSelectedTab() == -1) {
			selectTab(null);
		}
	}

}

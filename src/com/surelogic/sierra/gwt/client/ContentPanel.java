package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class ContentPanel extends Composite {
	private final DockPanel rootPanel = new DockPanel();
	private final LoginPanel loginPanel = new LoginPanel();
	private final TabsPanel tabsPanel = new TabsPanel();
	private final AdminPanel adminPanel = new AdminPanel();

	public static ContentPanel getInstance() {
		return (ContentPanel) RootPanel.get("content-pane").getWidget(0);
	}

	public ContentPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");
		rootPanel.setHeight("100%");
	}

	public void showDefault() {
		showAdmin();
	}
	
	public void showLogin(String errorMessage) {
		setContentPanel(loginPanel);
	}

	public void showTabs() {
		setContentPanel(tabsPanel);
	}

	public void showTab(String tabName) {
		showTabs();
		tabsPanel.selectTab(tabName);
	}

	public void showAdmin() {
		setContentPanel(adminPanel);
	}

	private void setContentPanel(Composite panel) {
		if (rootPanel.getWidgetIndex(panel) == -1) {
			rootPanel.clear();
			rootPanel.add(panel, DockPanel.CENTER);
		}
	}
}

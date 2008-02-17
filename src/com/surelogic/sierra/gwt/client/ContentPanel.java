package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class ContentPanel extends Composite {
	private final DockPanel rootPanel = new DockPanel();
	private ContentComposite currentContent;

	public static ContentPanel getInstance() {
		return (ContentPanel) RootPanel.get("content-pane").getWidget(0);
	}

	public ContentPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");
		rootPanel.setHeight("100%");
	}

	public void show(ContentComposite content) {
		if (currentContent == content) {
			return;
		}
		if (currentContent == null || currentContent.deactivate()) {
			currentContent = content;
			currentContent.activate();
			rootPanel.clear();
			rootPanel.add(currentContent, DockPanel.CENTER);
		}
	}

	public void showDefault() {
		show(AdminContent.getInstance());
	}

}

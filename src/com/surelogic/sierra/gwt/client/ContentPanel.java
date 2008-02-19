package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public class ContentPanel extends Composite implements ClientContextListener {
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
		ClientContext.addChangeListener(this);
	}

	public void show(ContentComposite content) {
		if (currentContent == content) {
			return;
		}
		if (currentContent == null || currentContent.deactivate()) {
			currentContent = content;
			rootPanel.clear();
			rootPanel.add(currentContent, DockPanel.CENTER);
			currentContent.activate();
		}
	}

	public void showDefault() {
		show(AdminContent.getInstance());
	}

	public void onChange(UserAccount account, String context) {
		if (context == null || "".equals(context)) {
			showDefault();
		} else if ("Preferences".equals(context)) {
			show(PrefsContent.getInstance());
		}
	}

}

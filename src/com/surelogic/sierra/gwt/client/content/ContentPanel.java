package com.surelogic.sierra.gwt.client.content;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextListener;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.SessionManager;
import com.surelogic.sierra.gwt.client.content.login.LoginContent;
import com.surelogic.sierra.gwt.client.content.overview.OverviewContent;

public class ContentPanel extends Composite implements ContextListener {
	private final DockPanel rootPanel = new DockPanel();
	private ContentComposite currentContent;

	public static ContentPanel getInstance() {
		return (ContentPanel) RootPanel.get("content-pane").getWidget(0);
	}

	public ContentPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");

		ContextManager.addContextListener(this);
	}

	public void onChange(final Context context) {
		ContentComposite content;
		if (!SessionManager.isLoggedIn()) {
			content = LoginContent.getInstance();
		} else {
			content = context.getContent();
			if (content == null) {
				content = OverviewContent.getInstance();
			}
		}
		if (currentContent == content) {
			currentContent.update(context);
			return;
		}
		if (currentContent != null) {
			currentContent.deactivate();
		}

		currentContent = content;
		rootPanel.clear();
		rootPanel.add(currentContent, DockPanel.CENTER);
		currentContent.update(context);
	}

}

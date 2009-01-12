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
import com.surelogic.sierra.gwt.client.content.settings.OneTimeSettingsForm;

public class ContentPane extends Composite implements ContextListener {
	private final DockPanel rootPanel = new DockPanel();
	private ContentComposite currentContent;

	public static ContentPane getInstance() {
		return (ContentPane) RootPanel.get("content-pane").getWidget(0);
	}

	public ContentPane() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");

		ContextManager.addContextListener(this);
	}

	public void onChange(final Context context) {
		ContentComposite content;
		if (!SessionManager.isLoggedIn()) {
			content = LoginContent.getInstance();
		} else if (SessionManager.getUser().showServerConfig()) {
			content = new OneTimeSettingsForm();
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

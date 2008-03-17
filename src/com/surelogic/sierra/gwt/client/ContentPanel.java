package com.surelogic.sierra.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class ContentPanel extends Composite implements ContextListener {
	private final DockPanel rootPanel = new DockPanel();
	private final Map contentRegistry = new HashMap();
	private ContentComposite currentContent;

	public static ContentPanel getInstance() {
		return (ContentPanel) RootPanel.get("content-pane").getWidget(0);
	}

	public ContentPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");

		// register Content instances here
		registerContent(LoginContent.getInstance());
		registerContent(OverviewContent.getInstance());
		registerContent(SettingsContent.getInstance());
		registerContent(UserManagementContent.getInstance());
		registerContent(FindingContent.getInstance());
		ClientContext.addContextListener(this);
	}

	public void onChange(String context) {
		if (!ClientContext.isLoggedIn()) {
			context = LoginContent.getInstance().getContextName();
		} else if (context == null || "".equals(context)) {
			context = OverviewContent.getInstance().getContextName();
		}

		ContentComposite content = (ContentComposite) contentRegistry
				.get(context.toLowerCase());
		if (content == null) {
			if (ClientContext.isLoggedIn()) {
				content = OverviewContent.getInstance();
			} else {
				content = LoginContent.getInstance();
			}
		}

		if (currentContent == content) {
			return;
		}
		if (currentContent == null || currentContent.deactivate()) {
			currentContent = content;
			rootPanel.clear();
			rootPanel.add(currentContent, DockPanel.CENTER);
			currentContent.activate(context);
		}
	}

	private void registerContent(ContentComposite content) {
		contentRegistry.put(content.getContextName().toLowerCase(), content);
	}
}

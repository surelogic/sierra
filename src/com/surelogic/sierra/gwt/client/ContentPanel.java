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
		registerContent(FilterSetContent.getInstance());
		ClientContext.addContextListener(this);
	}

	public void onChange(Context context) {

		ContentComposite content;
		if (!ClientContext.isLoggedIn()) {
			content = LoginContent.getInstance();
		} else {
			content = (ContentComposite) contentRegistry.get(context.getContent());
			if(content == null) {
				content = OverviewContent.getInstance();
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
		contentRegistry.put(content.getContentName().toLowerCase(), content);
	}
}

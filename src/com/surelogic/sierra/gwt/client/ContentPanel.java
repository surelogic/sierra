package com.surelogic.sierra.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public class ContentPanel extends Composite implements ClientContextListener {
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
		rootPanel.setHeight("100%");

		// register Content instances here
		registerContent(ProjectOverviewContent.getInstance());
		registerContent(ServerSettingsContent.getInstance());
		registerContent(UserManagementContent.getInstance());
		registerContent(ChangePasswordContent.getInstance());
		ClientContext.addChangeListener(this);
	}

	public void onChange(UserAccount account, String context) {
		ContentComposite content = null;
		if (account == null) {
			content = LoginContent.getInstance();
		} else {
			if (context != null && !"".equals(context)) {
				content = (ContentComposite) contentRegistry.get(context
						.toLowerCase());
			}

			if (content == null) {
				if (account.isAdministrator()) {
					ClientContext.setContext(ServerSettingsContent
							.getInstance().getContextName());
					return;
				} else {
					ClientContext.setContext(ChangePasswordContent
							.getInstance().getContextName());
					return;
				}
			}
		}

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

	private void registerContent(ContentComposite content) {
		contentRegistry.put(content.getContextName().toLowerCase(), content);
	}
}

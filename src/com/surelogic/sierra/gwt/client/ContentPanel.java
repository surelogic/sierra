package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;

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

		ClientContext.addContextListener(this);
	}

	public void onChange(Context context) {
		ContentComposite content;
		if (!ClientContext.isLoggedIn()) {
			content = LoginContent.getInstance();
		} else {
			content = context.getContent();
			if (content == null) {
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

}

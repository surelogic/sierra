package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.util.UI;

public class FindingContent extends ContentComposite {

	private static final FindingContent instance = new FindingContent();

	private HTML synopsis;

	private FindingContent() {
		// Do nothing
	}

	public String getContextName() {
		return "Finding";
	}

	protected void onActivate(String context) {
	}

	protected boolean onDeactivate() {
		return true;
	}

	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		panel.add(UI.h3("Synopsis"));
		panel.add(synopsis);
		getRootPanel().add(panel, DockPanel.CENTER);
	}

	public static FindingContent getInstance() {
		return instance;
	}
}

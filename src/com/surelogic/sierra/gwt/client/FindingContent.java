package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.util.UI;

public class FindingContent extends ContentComposite {

	private static final FindingContent instance = new FindingContent();

	private HTML synopsis = new HTML();

	private HTML location = new HTML();

	private HTML description = new HTML();

	private HTML audits = new HTML();
	
	private HTML artifacts = new HTML();

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
		panel.add(UI.h3("Location"));
		panel.add(location);
		panel.add(UI.h3("Description"));
		panel.add(description);
		panel.add(UI.h3("Audits"));
		panel.add(audits);
		panel.add(UI.h3("Artifacts"));
		panel.add(artifacts);
		getRootPanel().add(panel, DockPanel.CENTER);
	}

	public static FindingContent getInstance() {
		return instance;
	}
}

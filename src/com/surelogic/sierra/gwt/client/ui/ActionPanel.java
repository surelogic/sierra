package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class ActionPanel extends Composite {
	private static final String STYLE = "sl-ActionPanel";

	private final HorizontalPanel rootPanel = new HorizontalPanel();

	public ActionPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(STYLE);
	}

	public void addAction(String text, ClickListener actionListener) {
		final Label action = new Label(text);
		action.addStyleName(STYLE + "-item");
		action.addClickListener(actionListener);
		rootPanel.add(action);
	}
}

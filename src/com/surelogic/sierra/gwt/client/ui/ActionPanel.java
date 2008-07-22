package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ActionPanel extends Composite {
	private static final String STYLE = "sl-ActionPanel";
	private final Image waitImage = ImageHelper.getWaitImage(16);
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel actionPanel = new HorizontalPanel();

	public ActionPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(STYLE);

		rootPanel.add(actionPanel);
	}

	public void addAction(String text, ClickListener actionListener) {
		final Label action = new Label(text);
		action.addStyleName(STYLE + "-item");
		action.addClickListener(actionListener);
		actionPanel.add(action);
	}

	public void setWaitStatus() {
		rootPanel.remove(actionPanel);
		if (rootPanel.getWidgetIndex(waitImage) == -1) {
			rootPanel.add(waitImage);
		}
	}

	public void clearWaitStatus() {
		rootPanel.remove(waitImage);
		if (rootPanel.getWidgetIndex(actionPanel) == -1) {
			rootPanel.add(actionPanel);
		}
	}
}

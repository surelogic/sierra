package com.surelogic.sierra.gwt.client.ui.panel;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;

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

	public Label addAction(final String text, final ClickListener actionListener) {
		final Label action = new Label(text);
		action.addStyleName(STYLE + "-item");
		action.addClickListener(actionListener);
		actionPanel.add(action);
		return action;
	}

	public void removeAction(final Label action) {
		actionPanel.remove(action);
	}

	public void setActionVisible(final String actionText, final boolean visible) {
		for (int i = 0; i < actionPanel.getWidgetCount(); i++) {
			final Widget w = actionPanel.getWidget(i);
			if (w instanceof Label) {
				if (((Label) w).getText().equalsIgnoreCase(actionText)) {
					w.setVisible(visible);
				}
			}
		}
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

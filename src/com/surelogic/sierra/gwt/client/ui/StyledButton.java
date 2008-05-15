package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;

public class StyledButton extends Label {
	private static final String PRIMARY_STYLE = "sl-Button";

	public StyledButton(String text, ClickListener listener) {
		super(text);
		addStyleName(PRIMARY_STYLE);
		addStyleName("clickable2");
		addClickListener(listener);
	}
}

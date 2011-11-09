package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.CustomButton;

public class StyledButton extends CustomButton {
	private static final String PRIMARY_STYLE = "sl-Button";

	public StyledButton(String text) {
		super(text);
		setStyleName(PRIMARY_STYLE);
	}

	public StyledButton(String text, ClickListener listener) {
		super(text, listener);
		setStyleName(PRIMARY_STYLE);
	}
}

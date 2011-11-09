package com.surelogic.sierra.gwt.client.ui.dialog;

import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MessageDialog extends OkCancelDialog {
	private final Label prompt = new Label();

	public MessageDialog(final String title, final String width,
			final String message) {
		super(title, width);
		prompt.setWidth("100%");
		prompt.setText(message);
		prompt.addStyleName("padded");
	}

	@Override
	protected void doInitialize(final VerticalPanel contentPanel) {
		contentPanel.add(prompt);
	}

	@Override
	protected HasFocus getInitialFocus() {
		return null;
	}

}

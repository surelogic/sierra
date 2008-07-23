package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.TextBox;
import com.surelogic.sierra.gwt.client.ui.dialog.FormDialog;

public class CreateServerDialog extends FormDialog {

	TextBox label = new TextBox();

	@Override
	protected void doInitialize(final FlexTable contentTable) {
		setText("Choose A Label For The Server Location");
		setWidth("600px");
		addField("Label", label);
	}

	@Override
	protected HasFocus getInitialFocus() {
		return label;
	}

	public String getLabelText() {
		return label.getText();
	}
}

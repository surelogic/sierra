/**
 * 
 */
package com.surelogic.sierra.gwt.client.content.reports;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.TextBox;
import com.surelogic.sierra.gwt.client.ui.dialog.FormDialog;

public class SaveReportDialog extends FormDialog {
	private final TextBox name = new TextBox();

	public SaveReportDialog() {
		super("Save the current report settings as...", null);
	}

	public String getName() {
		return name.getText();
	}

	@Override
	protected void doInitialize(FlexTable contentTable) {
		addField("Report Name", name);
	}

	@Override
	protected HasFocus getInitialFocus() {
		return name;
	}

}

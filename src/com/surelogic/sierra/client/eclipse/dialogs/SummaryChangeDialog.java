package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.eclipse.FontUtility;
import com.surelogic.common.eclipse.SLImages;

public class SummaryChangeDialog extends Dialog {

	private String f_currentText;
	private Text f_summaryChangeText;

	public SummaryChangeDialog(Shell parentShell, String currentText) {
		super(parentShell);
		f_currentText = currentText;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Label summaryChangeLabel = new Label(composite, SWT.WRAP);
		summaryChangeLabel.setFont(FontUtility.getDefaultBoldFont());
		summaryChangeLabel.setText("Enter the new summary :");

		f_summaryChangeText = new Text(composite, SWT.BORDER | SWT.MULTI
				| SWT.V_SCROLL | SWT.WRAP);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
		layoutData.widthHint = 300;
		layoutData.heightHint = 50;
		f_summaryChangeText.setLayoutData(layoutData);
		f_summaryChangeText.setText(f_currentText);
		return super.createDialogArea(parent);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Sierra");
		shell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_LOGO));
	}

	@Override
	protected void okPressed() {
		f_currentText = f_summaryChangeText.getText();
		if (validateText(f_currentText)) {
			super.okPressed();
		} else {
			MessageDialog.openError(new Shell(), "Error",
					"Enter a valid text for summary");
		}
	}

	private boolean validateText(String text) {
		if (text.trim().length() != 0) {
			return true;
		}
		return false;
	}

	public String getText() {
		return f_currentText;
	}

}

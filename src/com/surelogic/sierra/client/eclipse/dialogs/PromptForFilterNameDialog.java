package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;

public final class PromptForFilterNameDialog extends MessageDialog {

	public PromptForFilterNameDialog(Shell parentShell, String message) {
		super(parentShell, "Send Local Scan Filter", SLImages
				.getImage(CommonImages.IMG_SIERRA_LOGO), message,
				MessageDialog.QUESTION, new String[] { "Send", "Cancel" }, 0);
	}

	private Text f_name;
	private String f_enteredName = "";

	/**
	 * Gets the text entered as the scan filter name. This should not be empty
	 * unless cancel was pressed.
	 * 
	 * @return the non-null scan filter name entered into the dialog.
	 */
	public String getText() {
		return f_enteredName;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		f_name = new Text(parent, SWT.SINGLE);
		f_name.setLayoutData(new GridData(GridData.FILL_BOTH));

		f_name.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				setButtonState();
			}
		});
		/*
		 * We have to run this a bit later so that the OK button is created.
		 */
		f_name.getDisplay().asyncExec(new Runnable() {
			public void run() {
				setButtonState();
			}
		});

		return f_name;
	}

	private void setButtonState() {
		if (f_name != null) {
			final String typed = f_name.getText();
			f_enteredName = typed;
			boolean tipTyped = typed.length() != 0;
			final Button ok = getButton(IDialogConstants.OK_ID);
			ok.setEnabled(tipTyped);
		}
	}
}

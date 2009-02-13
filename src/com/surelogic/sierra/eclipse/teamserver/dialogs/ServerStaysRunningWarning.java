package com.surelogic.sierra.eclipse.teamserver.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.eclipse.teamserver.preferences.PreferenceConstants;

public final class ServerStaysRunningWarning extends Dialog {

	private static final int f_widthHint = 350;

	public ServerStaysRunningWarning() {
		super(SWTUtility.getShell());
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_LOGO));
		newShell.setText("Local Team Server Warning");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite panel = (Composite) super.createDialogArea(parent);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		Label banner = new Label(panel, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
		banner.setImage(SLImages
				.getImage(CommonImages.IMG_SIERRA_POWERED_BY_SURELOGIC));

		final Composite msgPanel = new Composite(panel, SWT.NONE);
		msgPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		gridLayout = new GridLayout();
		msgPanel.setLayout(gridLayout);

		Label msg = new Label(msgPanel, SWT.WRAP);
		GridData data = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
		data.widthHint = f_widthHint;
		msg.setLayoutData(data);
		msg.setText(I18N.msg("sierra.eclipse.teamserver.keepsRunningWarning1"));

		msg = new Label(msgPanel, SWT.WRAP);
		data = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
		data.widthHint = f_widthHint;
		msg.setLayoutData(data);
		msg.setText(I18N.msg("sierra.eclipse.teamserver.keepsRunningWarning2"));

		msg = new Label(msgPanel, SWT.WRAP);
		data = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
		data.widthHint = f_widthHint;
		msg.setLayoutData(data);
		msg.setText(I18N.msg("sierra.eclipse.teamserver.keepsRunningWarning3"));

		final Button check = new Button(msgPanel, SWT.CHECK);
		data = new GridData(SWT.DEFAULT, SWT.BOTTOM, false, true);
		check.setLayoutData(data);
		check.setText("Please do not show this warning again");
		check.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				PreferenceConstants.setWarnAboutServerStaysRunning(!check
						.getSelection());
			}
		});

		return panel;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL,
				false);		
	}
}

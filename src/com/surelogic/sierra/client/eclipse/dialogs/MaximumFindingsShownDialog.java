package com.surelogic.sierra.client.eclipse.dialogs;

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
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public final class MaximumFindingsShownDialog extends Dialog {

	final private int f_findingsLimit;

	final private int f_findingsCount;

	private static final int f_widthHint = 350;

	public MaximumFindingsShownDialog(int findingsLimit, int findingsCount) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		f_findingsLimit = findingsLimit;
		f_findingsCount = findingsCount;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_LOGO));
		newShell.setText("Findings List Limited");
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
				.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC_GLOBE));

		final Composite msgPanel = new Composite(panel, SWT.NONE);
		msgPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		gridLayout = new GridLayout();
		msgPanel.setLayout(gridLayout);

		final String text = I18N.err(33, f_findingsCount, f_findingsLimit,
				f_findingsCount, f_findingsLimit);
		SLLogger.getLogger().warning(text);

		Label msg = new Label(msgPanel, SWT.WRAP);
		GridData data = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
		data.widthHint = f_widthHint;
		msg.setLayoutData(data);
		msg.setText(text);

		final Button check = new Button(msgPanel, SWT.CHECK);
		data = new GridData(SWT.DEFAULT, SWT.BOTTOM, false, true);
		check.setLayoutData(data);
		check.setText("Please do not show this warning again");
		check.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				PreferenceConstants.setWarnAboutMaximumFindingsShown(!check
						.getSelection());
			}
		});

		return panel;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}
}

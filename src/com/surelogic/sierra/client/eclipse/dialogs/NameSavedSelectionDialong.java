package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.eclipse.SLImages;

public final class NameSavedSelectionDialong extends Dialog {

	private String f_name = null;

	public String getName() {
		return f_name;
	}

	public NameSavedSelectionDialong(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_INVESTIGATE));
		newShell.setText("Name Selection");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		final Label directions = new Label(panel, SWT.NONE);
		directions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		directions.setText("Enter a name for the current selection");

		final Label label = new Label(panel, SWT.NONE);
		label.setText("Name:");
		label.setForeground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_BLUE));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		final Text name = new Text(panel, SWT.SINGLE);
		name.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				f_name = name.getText();
			}
		});
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		return panel;
	}
}

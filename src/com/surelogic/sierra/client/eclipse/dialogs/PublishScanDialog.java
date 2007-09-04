package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class PublishScanDialog extends Dialog {

	private final List<String> qualifiers;
	private Button qualifierButtons[];
	private Vector<String> qualifierNames;

	public PublishScanDialog(Shell parentShell, List<String> qualifiers) {
		super(parentShell);
		this.qualifiers = qualifiers;

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Font font = parent.getFont();
		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 1;
		composite.setFont(font);

		Iterator<String> qualifierIterator = qualifiers.iterator();

		qualifierButtons = new Button[qualifiers.size()];

		for (int i = 0; qualifierIterator.hasNext(); i++) {

			String qualifierName = qualifierIterator.next();
			qualifierButtons[i] = new Button(composite, SWT.CHECK);
			qualifierButtons[i].setText(qualifierName);

		}

		return composite;
	}

	@Override
	protected void okPressed() {
		qualifierNames = new Vector<String>();
		for (int i = 0; i < qualifierButtons.length; i++) {
			if (qualifierButtons[i].getSelection()) {
				qualifierNames.add(qualifierButtons[i].getText());
			}
		}
		super.okPressed();

	}

	public Vector<String> getNames() {
		return qualifierNames;
	}

}

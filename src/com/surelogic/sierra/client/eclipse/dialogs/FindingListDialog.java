package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.sierra.client.eclipse.views.FindingsDetailsView;

public class FindingListDialog extends Dialog {

	private Map<Long, String> f_findingsMap = new HashMap<Long, String>();
	private List f_findingsList;

	public FindingListDialog(Shell shell, Map<Long, String> findings) {
		super(shell);
		f_findingsMap = findings;
	}

	private long getValue(String text) {
		if (text != null) {
			Set<Long> findingIds = f_findingsMap.keySet();
			for (Long l : findingIds) {
				String holder = f_findingsMap.get(l);
				if (holder.equals(text)) {
					return l;
				}
			}
		}

		return -1;
	}

	@Override
	protected void okPressed() {
		if (f_findingsList != null) {
			String summary = f_findingsList.getSelection()[0];
			long id = getValue(summary);

			if (id != -1) {
				FindingsDetailsView view = (FindingsDetailsView) ViewUtility
						.showView("com.surelogic.sierra.client.eclipse.views.FindingsDetailsView");
				view.findingSelected(id);
			}
		}
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Sierra");
		shell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_LOGO));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Label multipleMarker = new Label(composite, SWT.NONE);
		multipleMarker.setText("Multiple findings on the line, select "
				+ "one to view details :");
		f_findingsList = new List(composite, SWT.WRAP | SWT.BORDER);

		f_findingsList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		Set<Long> findingIds = f_findingsMap.keySet();
		for (Long l : findingIds) {
			String text = f_findingsMap.get(l);
			f_findingsList.add(text);
		}

		f_findingsList.setSelection(0);

		f_findingsList.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
				okPressed();
			}

			public void mouseDown(MouseEvent e) {
				// Nothing to do

			}

			public void mouseUp(MouseEvent e) {
				// Nothing to do

			}

		});
		return super.createDialogArea(parent);
	}
}

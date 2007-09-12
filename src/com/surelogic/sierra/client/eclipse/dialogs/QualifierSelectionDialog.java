package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.common.eclipse.SLImages;

public final class QualifierSelectionDialog extends Dialog {

	private final Set<String> f_qualifiers;

	private final Set<String> f_selectedQualifiers = new HashSet<String>();

	public Set<String> getSelectedQualifiers() {
		return new HashSet<String>(f_selectedQualifiers);
	}

	private final String f_projectName;

	private final String f_serverLabel;

	private Table f_qualifierTable;

	private boolean f_useForRemainingOnSameServer = true;

	public boolean useForAllOnSameServer() {
		return f_useForRemainingOnSameServer;
	}

	public QualifierSelectionDialog(Shell parentShell, Set<String> qualifiers,
			String projectName, String serverLabel) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		if (qualifiers == null || qualifiers.size() < 1)
			throw new IllegalArgumentException(
					"Qualifier set must be non-null and contain at least one qualifier");
		f_qualifiers = new HashSet<String>(qualifiers);
		if (projectName == null)
			throw new IllegalArgumentException("Project name must be non-null");
		f_projectName = projectName;
		if (serverLabel == null)
			throw new IllegalArgumentException("Server label must be non-null");
		f_serverLabel = serverLabel;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
		newShell.setText("Select Qualifiers");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		Label banner = new Label(panel, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, true, 1,
				1));
		banner.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC));

		final Composite entryPanel = new Composite(panel, SWT.NONE);
		entryPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		gridLayout = new GridLayout();
		entryPanel.setLayout(gridLayout);

		final Label l = new Label(entryPanel, SWT.WRAP);
		l.setText("Select '" + f_serverLabel + "' qualifiers to share '"
				+ f_projectName + "' to:");

		f_qualifierTable = new Table(entryPanel, SWT.MULTI);
		f_qualifierTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		final List<String> qualifiers = new ArrayList<String>(f_qualifiers);
		Collections.sort(qualifiers);

		for (String qualifier : qualifiers) {
			TableItem item = new TableItem(f_qualifierTable, SWT.NONE);
			item.setText(qualifier);
		}

		final Button useForAll = new Button(entryPanel, SWT.CHECK);
		useForAll.setText("Use for all remaining projects connected to '"
				+ f_projectName + "'");
		useForAll.setSelection(f_useForRemainingOnSameServer);
		useForAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_useForRemainingOnSameServer = useForAll.getSelection();
			}
		});

		f_qualifierTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_selectedQualifiers.clear();
				final TableItem[] sa = f_qualifierTable.getSelection();
				for (TableItem item : sa) {
					f_selectedQualifiers.add(item.getText());
				}
				setOKState();
			}
		});

		// add controls to composite as necessary
		return panel;
	}

	@Override
	protected Control createContents(Composite parent) {
		final Control contents = super.createContents(parent);
		setOKState();
		return contents;
	}

	private void setOKState() {
		getButton(IDialogConstants.OK_ID).setEnabled(
				f_selectedQualifiers.size() > 0);
	}
}

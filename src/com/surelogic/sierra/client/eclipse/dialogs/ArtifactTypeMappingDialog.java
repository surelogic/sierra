package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.tool.ArtifactType;

public class ArtifactTypeMappingDialog extends Dialog {
	private final Collection<ArtifactType> types;
	private Table f_mappingTable;	
	
	public ArtifactTypeMappingDialog(Shell parentShell, Collection<ArtifactType> types) {
		super(parentShell);
		this.types = types;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected final void configureShell(Shell newShell) {
		super.configureShell(newShell);
		//newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));
		newShell.setText("Map Artifact Type(s)");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		/*
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);
        */

		final Composite entryPanel = new Composite(panel, SWT.NONE);
		entryPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		gridLayout = new GridLayout();
		entryPanel.setLayout(gridLayout);

		final Label l = new Label(entryPanel, SWT.WRAP);
		l.setText("Map the artifact types below to an appropriate finding type");

		f_mappingTable = new Table(entryPanel, SWT.FULL_SELECTION);
		f_mappingTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		f_mappingTable.setHeaderVisible(true);
		final TableColumn column0 = new TableColumn(f_mappingTable, SWT.NONE);
	    column0.setText("Artifact Type");
	    final TableColumn column1 = new TableColumn(f_mappingTable, SWT.NONE);
	    column1.setText("Finding Type");
		// Setup table
		for(ArtifactType t : types) {
			TableItem item = new TableItem(f_mappingTable, SWT.NONE);
			item.setText(0, t.type);
			//item.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));
			item.setData(t);			
		}
		addToEntryPanel(entryPanel);

		f_mappingTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final TableItem[] sa = f_mappingTable.getSelection();
				if (sa.length > 0) {
					final TableItem selection = sa[0];
					//f_server = (ConnectedServer) selection.getData();
				}
				setOKState();
			}
		});

		// add controls to composite as necessary
		return panel;
	}

	protected void addToEntryPanel(Composite entryPanel) {
		// Do nothing
	}

	@Override
	protected final Control createContents(Composite parent) {
		final Control contents = super.createContents(parent);
		setOKState();
		return contents;
	}

	private final void setOKState() {
		//getButton(IDialogConstants.OK_ID).setEnabled(f_server != null);
	}

	public final boolean confirmNonnullServer() {
		/*
		if (f_server == null) {
			final int errNo = 18;
			final String msg = I18N.err(errNo);
			final IStatus reason = SLEclipseStatusUtility.createErrorStatus(errNo, msg);
			ErrorDialogUtility.open(getParentShell(),
					"Sierra Team Server must be non-null", reason);
			return false;
		}
		*/
		return true;
	}
}

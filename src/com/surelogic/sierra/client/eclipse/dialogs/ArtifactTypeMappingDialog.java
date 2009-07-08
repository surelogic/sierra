package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.*;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.jdbc.tool.*;
import com.surelogic.sierra.tool.ArtifactType;

public class ArtifactTypeMappingDialog extends Dialog {
	// editing the second column
	private final int EDITABLECOLUMN = 1;
	
	private final Collection<ArtifactType> types;
	private final List<FindingTypeDO> findingTypes;
	private Table f_mappingTable;	
	
	public ArtifactTypeMappingDialog(Shell parentShell, Collection<ArtifactType> types, List<FindingTypeDO> ft) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		
		this.types = types;
		findingTypes = ft;
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
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

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
		
	    // Setup table editor
	    final TableEditor editor = new TableEditor(f_mappingTable);
		//The editor must have the same size as the cell and must
		//not be any smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
		
		// Setup table
		for(ArtifactType t : types) {
			TableItem item = new TableItem(f_mappingTable, SWT.NONE);
			item.setText(t.type);
			item.setText(0, t.type);
			item.setText(1, lookupFindingTypeName(t.getFindingType()));
			item.setData(t);			
		}
		addToEntryPanel(entryPanel);
		column0.pack();
		column1.pack();
		
		f_mappingTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Clean up any previous editor control
				Control oldEditor = editor.getEditor();
				if (oldEditor != null) oldEditor.dispose();
		
				// Identify the selected row
				TableItem item = (TableItem)e.item;
				if (item == null) return;
				
				// The control that will be the editor must be a child of the Table
			    final Combo newEditor = new Combo(f_mappingTable, SWT.CHECK);
			    final ArtifactType type = (ArtifactType) item.getData();
			    
			    // Setup list of available finding types
			    for(FindingTypeDO f : findingTypes) {
			    	newEditor.add(f.getName());
			    }			
				newEditor.setText(type.getFindingType());
				newEditor.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						Combo text = (Combo)editor.getEditor();
						String name = text.getText();
						type.setFindingType(lookupFindingType(name));						
						editor.getItem().setText(EDITABLECOLUMN, name);
						setOKState();
					}
				});				
				newEditor.setFocus();
				newEditor.pack();
				editor.setEditor(newEditor, item, EDITABLECOLUMN);
				column1.pack();
			}
		});

		// add controls to composite as necessary
		return panel;
	}

	private String lookupFindingTypeName(String type) {
		for(FindingTypeDO f : findingTypes) {
			if (f.getUid().equals(type)) {
				return f.getName();
			}
		}
		return "";
	}
	
	private String lookupFindingType(String name) {
		for(FindingTypeDO f : findingTypes) {
			if (f.getName().equals(name)) {
				return f.getUid();
			}
		}
		return null;
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
		boolean allHaveFindingTypes = true;
		for(ArtifactType t : types) {
			if (t.getFindingType() == null) {
				allHaveFindingTypes = false;
				break;
			}
		}
		getButton(IDialogConstants.OK_ID).setEnabled(allHaveFindingTypes);
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

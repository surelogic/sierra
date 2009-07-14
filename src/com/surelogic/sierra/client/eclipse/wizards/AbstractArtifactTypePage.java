package com.surelogic.sierra.client.eclipse.wizards;

import java.util.Collection;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.sierra.tool.ArtifactType;

abstract class AbstractArtifactTypePage extends WizardPage {
	// editing the second column
	private static final int EDITABLECOLUMN = 1;
	static final String DEFAULT = "<create>";
	
	protected final Collection<ArtifactType> types;
	private final String columnName;
	private Table f_mappingTable;
	
	protected AbstractArtifactTypePage(String id, Collection<ArtifactType> t, String column) {
		super(id);
		types = t;
		columnName = column;
		
		setPageComplete(false);
	}

	public void createControl(Composite panel) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		final Composite entryPanel = new Composite(panel, SWT.NONE);
		entryPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		gridLayout = new GridLayout();
		entryPanel.setLayout(gridLayout);

		f_mappingTable = new Table(entryPanel, SWT.FULL_SELECTION);
		f_mappingTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		f_mappingTable.setHeaderVisible(true);		
		final TableColumn column0 = new TableColumn(f_mappingTable, SWT.NONE);
	    column0.setText("Artifact Type");
	    final TableColumn column1 = new TableColumn(f_mappingTable, SWT.NONE);
	    column1.setText(columnName);
		
	    // Setup table editor
	    final TableEditor editor = new TableEditor(f_mappingTable);
		//The editor must have the same size as the cell and must
		//not be any smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
		
		update();
		for(TableColumn c : f_mappingTable.getColumns()) {
			c.pack();
		}
		
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
			    newEditor.add(DEFAULT);	
			    initCombo(newEditor);
			    
			    if (type.getFindingType() != null) {
					newEditor.setText(convertToName(type));
			    } else {
			    	newEditor.setText(DEFAULT);
			    }
				newEditor.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						Combo text = (Combo)editor.getEditor();
						String name = text.getText();
						convertFromName(type, name);						
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
		finishTableInit(f_mappingTable);
		setControl(entryPanel);
	}
	protected void finishTableInit(Table t) {
		// Nothing to do yet
	}
	
	protected void update() {
		f_mappingTable.removeAll();
		
		// Setup table
		for(ArtifactType t : types) {
			if (showType(t)) {
				TableItem item = new TableItem(f_mappingTable, SWT.NONE);
				item.setText(t.type);
				item.setText(0, t.type);
				item.setText(1, convertToName(t));
				item.setData(t);
				updateRest(t, item);
			}
		}
		setOKState();
	}
	
	protected void updateRest(ArtifactType t, TableItem item) {
		// Nothing to do yet
	}

	protected boolean showType(ArtifactType t) {
		return true;
	}

	protected abstract void initCombo(Combo c);	
	protected abstract String convertFromName(ArtifactType t, String name);
	protected abstract String convertToName(ArtifactType t);
	protected abstract void setOKState();
}

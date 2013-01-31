package com.surelogic.sierra.client.eclipse.wizards;

import java.util.*;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.surelogic.sierra.tool.ArtifactType;

public class ManifestLocationPage extends WizardPage {
	// editing the second column
	private static final int EDITABLECOLUMN = 1;
	
	private final Map<String,String> locationMap = new HashMap<String,String>();
	private Table f_mappingTable;
	
	protected ManifestLocationPage(Map<String, List<ArtifactType>> plugins) {
		super("ManifestLocationPage");
		setPageComplete(false);
		setTitle("Select Manifest Location");
		setDescription("Select an output location for the generated manifest");
		for(String id : plugins.keySet()) {
			locationMap.put(id, null);
		}
	}

	@Override
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
	    column0.setText("Tool Extension");
	    final TableColumn column1 = new TableColumn(f_mappingTable, SWT.NONE);
	    column1.setText("Manifest Location");
		
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
			    final Button newEditor = new Button(f_mappingTable, SWT.NONE);
			    final String id = (String) item.getData();
			    String path = locationMap.get(id);
			    if (path == null) {
			    	newEditor.setText("Browse...");			    	
			    } else {
			    	newEditor.setText(path);
			    }
			    
				newEditor.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
				        FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
				        fd.setText("Save As");		        
				        //fd.setFilterPath("C:/");
				        //String[] filterExt = { "*.txt", "*.doc", ".rtf", "*.*" };
				        //fd.setFilterExtensions(filterExt);
				        String path = fd.open();
				        if (path != null) {
				        	locationMap.put(id, path);
				        	newEditor.setText(path);
				    		editor.getItem().setText(EDITABLECOLUMN, path);
							setOKState();
				        }		
					}
				});				
				newEditor.setFocus();
				newEditor.pack();
				editor.setEditor(newEditor, item, EDITABLECOLUMN);
				column1.pack();
			}
		});
		setControl(entryPanel);
	}
	
	public Map<String,String> getLocationMap() {
		return locationMap;
	}

	void update() {
		if (f_mappingTable == null) {
			return;
		}
		f_mappingTable.removeAll();
		
		// Setup table
		for(Map.Entry<String, String> e : locationMap.entrySet()) {
			TableItem item = new TableItem(f_mappingTable, SWT.NONE);
			item.setText(e.getKey());
			item.setText(0, e.getKey());
			item.setText(1, e.getValue() == null ? "" : e.getValue());
			item.setData(e.getKey());			
		}
		setOKState();
	}	
	
	private void setOKState() {
		boolean allHaveLocations = true;
		for(Map.Entry<String, String> e : locationMap.entrySet()) {
			if (e.getValue() == null) {
				allHaveLocations = false;
				break;
			}
		}		
		setPageComplete(allHaveLocations);
	}
}

package com.surelogic.sierra.client.eclipse.wizards;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.tool.ArtifactType;

public class FindingTypeSetupPage extends AbstractArtifactTypePage {
	private static final int FILTER_COLUMN= 2;
	private static final String DESCRIPTION = "Setup categories and scan filter defaults for new types";
	private static final String NOTHING = "No new finding types to setup";
	
	private final List<CategoryDO> categories;
	
	protected FindingTypeSetupPage(Collection<ArtifactType> t, List<CategoryDO> cats) {
		super("FindingTypeSetupPage", t, "Category");
		categories = cats;

		setTitle("Setup New Finding Types");
		setDescription(DESCRIPTION);
	}

	@Override
	protected void finishTableInit(final Table t) {
		final TableColumn column = new TableColumn(t, SWT.NONE);
		column.setText("Include in Filter");
		column.pack();
		
		 // Setup table editor
	    final TableEditor editor = new TableEditor(t);
		//The editor must have the same size as the cell and must
		//not be any smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
		
		t.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Clean up any previous editor control
				Control oldEditor = editor.getEditor();
				if (oldEditor != null) oldEditor.dispose();
		
				// Identify the selected row
				TableItem item = (TableItem)e.item;
				if (item == null) return;
				
				// The control that will be the editor must be a child of the Table
			    final Button newEditor = new Button(t, SWT.CHECK);
			    final ArtifactType type = (ArtifactType) item.getData();
			 
			    newEditor.setSelection(type.includeInScan());
			    
				newEditor.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button check = (Button)editor.getEditor();
						type.includeInScan(check.getSelection());												
						//setOKState();
					}
				});				
				newEditor.setFocus();
				newEditor.pack();
				editor.setEditor(newEditor, item, FILTER_COLUMN);
				column.pack();
			}
		});
	}
	
	@Override
	protected void updateRest(ArtifactType t, TableItem item) {
		item.setText(FILTER_COLUMN, t.includeInScan() ? "Include" : "Ignore");
	}
	
	@Override
	protected boolean showType(ArtifactType t) {
		return DEFAULT.equals(t.getFindingType());
	}
	
	@Override
	protected void initCombo(Combo c) {
		for(CategoryDO cat : categories) {
			c.add(cat.getName());
		}
	}
	
	@Override
	protected String convertFromName(ArtifactType t, String name) {
		for(CategoryDO f : categories) {
			if (f.getName().equals(name)) {
				return t.setCategory(f.getUid());
			}
		}
		return t.setCategory(name);

	}
	
	@Override
	protected String convertToName(ArtifactType t) {	
		final String id = t.getCategory();
		for(CategoryDO f : categories) {
			if (f.getUid().equals(id)) {
				return f.getName();
			}
		}
		return id;
	}

	@Override
	protected void setOKState() {
		boolean nothingToDo = true;
		boolean allHaveCategories = true;
		for(ArtifactType t : types) {
			if (showType(t)) {
				nothingToDo = false;
				
				if (t.getCategory() == null) {			
					allHaveCategories = false;
					break;
				}
			}
		}
		setDescription(nothingToDo ? NOTHING : DESCRIPTION);
		setPageComplete(allHaveCategories);
		if (allHaveCategories) {
			((ManifestLocationPage) getNextPage()).update();
		}
	}
}

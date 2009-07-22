package com.surelogic.sierra.client.eclipse.wizards;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.tool.ArtifactType;

public class FindingTypeSetupPage extends AbstractArtifactTypePage {
	private static final int FILTER_COLUMN = 2;
	private static final String DESCRIPTION = "Setup categories and scan filter defaults for new types";
	private static final String NOTHING = "No new finding types to setup";

	private final List<CategoryDO> categories;

	protected FindingTypeSetupPage(final Collection<ArtifactType> t,
			final List<CategoryDO> cats) {
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
		// The editor must have the same size as the cell and must
		// not be any smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;

		t.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				// Clean up any previous editor control
				final Control oldEditor = editor.getEditor();
				if (oldEditor != null) {
					oldEditor.dispose();
				}

				// Identify the selected row
				final TableItem item = (TableItem) e.item;
				if (item == null) {
					return;
				}

				// The control that will be the editor must be a child of the
				// Table
				final Button newEditor = new Button(t, SWT.CHECK);
				final ArtifactType type = (ArtifactType) item.getData();

				newEditor.setSelection(type.includeInScan());

				newEditor.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						final Button check = (Button) editor.getEditor();
						type.includeInScan(check.getSelection());
						// setOKState();
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
	protected void updateRest(final ArtifactType t, final TableItem item) {
		item.setText(FILTER_COLUMN, t.includeInScan() ? "Include" : "Ignore");
	}

	@Override
	protected boolean showType(final ArtifactType t) {
		return null == t.getFindingType();
	}

	@Override
	protected void initCombo(final Combo c) {
		for (final CategoryDO cat : categories) {
			c.add(cat.getName());
		}
	}

	@Override
	protected String convertFromName(final ArtifactType t, final String name) {
		for (final CategoryDO f : categories) {
			if (f.getName().equals(name)) {
				return t.setCategory(f.getUid());
			}
		}
		return t.setCategory(name);

	}

	@Override
	protected String convertToName(final ArtifactType t) {
		final String id = t.getCategory();
		for (final CategoryDO f : categories) {
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
		for (final ArtifactType t : types) {
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

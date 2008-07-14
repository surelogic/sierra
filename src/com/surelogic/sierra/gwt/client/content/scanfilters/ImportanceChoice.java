package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.ui.ListBox;
import com.surelogic.sierra.gwt.client.data.ImportanceView;

public class ImportanceChoice extends ListBox {

	public ImportanceChoice() {
		this(false);
	}

	public ImportanceChoice(final boolean allowMultiples) {
		super();
		addItem("Default");
		final ImportanceView[] arr = ImportanceView.values();
		for (int i = 0; i < arr.length; i++) {
			addItem(arr[i].getName());
		}
		setMultipleSelect(allowMultiples);
	}

	/**
	 * Returns all of the selected importance values. If 'Default' is selected,
	 * it will return all importance values that belong to the default set.
	 * 
	 * @return
	 */
	public Set<ImportanceView> getSelectedImportances() {
		final Set<ImportanceView> selected = new HashSet<ImportanceView>();
		if (isItemSelected(1)) {
			selected.addAll(ImportanceView.standardValues());
		}
		for (int i = 1; i <= ImportanceView.values().length; i++) {
			if (isItemSelected(i)) {
				selected.add(ImportanceView.values()[i - 1]);
			}
		}
		return selected;
	}

	/**
	 * Return a single selected importance, or <code>null</code> if 'Default' is
	 * selected.
	 * 
	 * @return
	 */
	public ImportanceView getSelectedImportance() {
		final String selected = getItemText(getSelectedIndex());
		if ("Default".equals(selected)) {
			return null;
		}
		return ImportanceView.fromString(selected);
	}

	public void setSelectedImportance(final ImportanceView imp) {
		if (imp == null) {
			setSelectedIndex(0);
		} else {
			final ImportanceView[] arr = ImportanceView.values();
			for (int i = 0; i < arr.length; i++) {
				if (imp == arr[i]) {
					setSelectedIndex(i + 1);
				}
			}
		}
	}

}

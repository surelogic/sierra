package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.ui.ListBox;
import com.surelogic.sierra.gwt.client.data.ImportanceView;

public class ImportanceChoice extends ListBox {

	public ImportanceChoice() {
		super();
		addItem("Default");
		final ImportanceView[] arr = ImportanceView.values();
		for (int i = 0; i < arr.length; i++) {
			addItem(arr[i].getName());
		}
	}

	public ImportanceView getSelectedImportance() {
		final String selected = getItemText(getSelectedIndex());
		if ("Default".equals(selected)) {
			return null;
		}
		return ImportanceView.fromString(selected);
	}

	public void setSelectedImportance(ImportanceView imp) {
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

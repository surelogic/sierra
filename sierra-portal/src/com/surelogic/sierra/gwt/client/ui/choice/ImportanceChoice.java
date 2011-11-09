package com.surelogic.sierra.gwt.client.ui.choice;

import com.google.gwt.user.client.ui.ListBox;
import com.surelogic.sierra.gwt.client.data.ImportanceView;

abstract class ImportanceChoice extends ListBox {

	protected ImportanceChoice(final boolean allowMultiples) {
		super(allowMultiples);
		addItem("Default");
		final ImportanceView[] arr = ImportanceView.values();
		for (final ImportanceView element2 : arr) {
			addItem(element2.getName());
		}
		setSelectedIndex(0);
	}

}

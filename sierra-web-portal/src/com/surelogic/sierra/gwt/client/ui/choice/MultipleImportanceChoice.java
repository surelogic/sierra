package com.surelogic.sierra.gwt.client.ui.choice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.surelogic.sierra.gwt.client.data.ImportanceView;

public class MultipleImportanceChoice extends ImportanceChoice {

	public MultipleImportanceChoice() {
		super(true);
	}

	/**
	 * Returns all of the selected importance values. If 'Default' is selected,
	 * it will return all importance values that belong to the default set.
	 * 
	 * @return
	 */
	public Set<ImportanceView> getSelectedImportances() {
		final Set<ImportanceView> selected = new HashSet<ImportanceView>();
		if (isItemSelected(0)) {
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
	 * Set all importances in the list as selected.
	 * 
	 * @param importances
	 *            may be <code>null</code> or empty. In both cases, 'Default' is
	 *            selected.
	 */
	public void setSelectedImportances(final List<ImportanceView> importances) {
		if (importances == null || importances.isEmpty()) {
			setSelectedIndex(0);
		} else {
			setItemSelected(0, false);
			for (final ImportanceView imp : ImportanceView.values()) {
				setItemSelected(imp.ordinal() + 1, importances.contains(imp));
			}
		}
	}

}

package com.surelogic.sierra.gwt.client.ui;

import com.surelogic.sierra.gwt.client.data.ImportanceView;

public class SingleImportanceChoice extends ImportanceChoice {

	public SingleImportanceChoice() {
		super(false);
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

	/**
	 * Set a single importance as selected.
	 * 
	 * @param imp
	 *            may be <code>null</code>. If so, 'Default' is selected.
	 */
	public void setSelectedImportance(final ImportanceView imp) {
		if (imp == null) {
			setSelectedIndex(0);
		} else {
			setSelectedIndex(imp.ordinal() + 1);
		}
	}

}

package com.surelogic.sierra.client.eclipse.model.selection;

public interface ISelectionObserver {

	/**
	 * Indicates a change to the number of findings that this selection is
	 * allowing through itself.
	 * 
	 * @param selection
	 *            a findings selection.
	 */
	void selectionChanged(Selection selection);
	
	void columnsChanged(Selection selection, Column c);
}

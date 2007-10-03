package com.surelogic.sierra.client.eclipse.model.selection;

public interface ISelectionObserver {

	/**
	 * Indicates that the structure of this findings selection has changed. This
	 * means that one or more filters was added or removed from the selection.
	 * 
	 * @param selection
	 *            a findings selection.
	 */
	void selectionStructureChanged(Selection selection);
}

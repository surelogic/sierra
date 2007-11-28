package com.surelogic.sierra.client.eclipse.model.selection;

public interface ISelectionObserver {

	/**
	 * Indicates a change to the number of findings that this selection is
	 * allowing through itself.
	 * 
	 * @param selection
	 *            a findings selection.
	 */
	void selectionChanged(Selection selecton);

	/**
	 * Indicates that the structure of this findings selection has changed. This
	 * means that one or more filters was added or removed from the selection.
	 * <p>
	 * Any time a call is made to this method,
	 * {@link #selectionChanged(Selection)} will also be called.
	 * 
	 * @param selection
	 *            a findings selection.
	 */
	void selectionStructureChanged(Selection selection);
}

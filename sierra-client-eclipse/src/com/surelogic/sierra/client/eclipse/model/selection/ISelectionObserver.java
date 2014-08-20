package com.surelogic.sierra.client.eclipse.model.selection;

public interface ISelectionObserver {

  /**
   * Indicates a change to the number of findings that this selection is
   * allowing through itself.
   * 
   * @param selection
   *          a findings selection.
   */
  void selectionChanged(Selection selection);

  /**
   * Indicates a change to the visibility of a column in the list of findings
   * (the 'Show' results).
   * 
   * @param selection
   *          a findings selection
   * @param c
   *          the column that changed its visibility.
   */
  void columnVisibilityChanged(Selection selection, Column c);
}

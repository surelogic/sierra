package com.surelogic.sierra.client.eclipse.model.selection;

import com.surelogic.Nullable;

/**
 * Implemented to receive notifications from the Sierra {@link SelectionManager}
 * singleton.
 */
public interface ISelectionManagerObserver {

  /**
   * Called when the set of saved selections changes. This includes when the
   * saved selections are first loaded into Eclipse, when a new selection saved
   * under a name, and when a named selection is removed.
   */
  void savedSelectionsChanged();

  /**
   * Called when the working selection for Sierra changes.
   * 
   * @param newWorkingSelection
   *          the new working selection, may be {@code null}.
   * @param oldWorkingSelection
   *          the previous, or old, working selection, may be {@code null}.
   */
  void workingSelectionChanged(@Nullable Selection newWorkingSelection, @Nullable Selection oldWorkingSelection);

  /**
   * Implemented to observe the counts of findings being displayed and if that
   * is being limited by the preferences set for Sierra queries. Invoked when
   * the display of Sierra findings changes. This method reports how many
   * findings are being displayed and what the possible number that could be
   * displayed. It is used to warn the user that not all the findings are being
   * displayed.
   * <p>
   * If all the findings are being displayed then <tt>count == ofPossible</tt>.
   * 
   * @param count
   *          the number of findings being displayed to the Sierra user in
   *          Eclipse.
   * @param ofPossible
   *          the number of findings in the database that could be displayed.
   */
  void showingFindings(int count, int ofPossible);
}

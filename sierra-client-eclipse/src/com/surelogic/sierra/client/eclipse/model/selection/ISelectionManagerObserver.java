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
}

package com.surelogic.sierra.client.eclipse.model;

/**
 * Implemented to allow observation of when projects are added or removed from
 * the Sierra database.
 */
public interface IProjectsObserver {

  /**
   * Notifies an observer that a project has been added to or deleted from the
   * database.
   * <p>
   * Note that this call is <b>not</b> be made from a UI thread.
   * 
   * @param p
   *          the project manager object.
   */
  void notify(Projects p);
}

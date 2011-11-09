package com.surelogic.sierra.client.eclipse.views.selection;

public interface IFindingsObserver {
  void findingsLimited(boolean isLimited);
  void findingsDisposed();
}

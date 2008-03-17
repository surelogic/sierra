package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.IAction;

public interface IViewCallback {
  void hasData(boolean yes);
  boolean showingData();
  
  void setGlobalActionHandler(String id, IAction action);
}

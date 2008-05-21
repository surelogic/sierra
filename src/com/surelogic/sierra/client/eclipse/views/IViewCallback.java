package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.*;

public interface IViewCallback {
	enum Status {
		NO_DATA, WAITING_FOR_DATA, DATA_READY
	}	
	
	Status getStatus();
	void setStatus(Status s);
	boolean matchesStatus(boolean showing);
	void hasData(boolean yes);
  
  void setGlobalActionHandler(String id, IAction action);
  void addToViewMenu(IAction action);
  void addToActionBar(IAction action);
}

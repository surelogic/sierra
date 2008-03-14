package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.widgets.Listener;

public interface IViewMediator {
	void init();
	String getNoDataId();
	Listener getNoDataListener();
	String getHelpId();
	
	void setFocus();
	void dispose();
}

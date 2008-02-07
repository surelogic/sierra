package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;

public abstract class ContentComposite extends Composite {
	private final DockPanel rootPanel = new DockPanel();
	
	public ContentComposite() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");
		rootPanel.setHeight("100%");
	}
	
	public DockPanel getRootPanel() {
		return rootPanel;
	}
	
	public abstract void activate();
	
}

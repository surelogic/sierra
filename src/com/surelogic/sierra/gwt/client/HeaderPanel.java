package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Image;

public class HeaderPanel  extends Composite {
	private final DockPanel rootPanel = new DockPanel();
	
	public HeaderPanel() {
		super();
		initWidget(rootPanel);
		
		rootPanel.setVerticalAlignment(DockPanel.ALIGN_MIDDLE);
		rootPanel.setWidth("100%");
		
		Image sierraLogo = new Image(GWT.getModuleBaseURL() + "images/header-sierra-logo.gif");
		rootPanel.add(sierraLogo, DockPanel.WEST);
	}
}

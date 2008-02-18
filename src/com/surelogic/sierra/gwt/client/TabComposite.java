package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;

// TODO convert so TabComposite has on* methods for UI creation, etc like ContentComposite
public abstract class TabComposite extends Composite {
	private final DockPanel rootPanel = new DockPanel();

	public TabComposite() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName("sl-TabComposite");
	}

	public abstract String getName();

	public DockPanel getRootPanel() {
		return rootPanel;
	}

}

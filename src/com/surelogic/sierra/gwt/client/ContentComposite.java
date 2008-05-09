package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.DockPanel;

public abstract class ContentComposite extends LifecycleComposite {
	private final DockPanel rootPanel = new DockPanel();

	public ContentComposite() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName("sl-ContentComposite");
	}

	public final void show() {
		ContextManager.setContent(this);
	}

	protected final void onInitialize() {
		onInitialize(rootPanel);
	}

	protected final DockPanel getRootPanel() {
		return rootPanel;
	}

	protected abstract void onInitialize(DockPanel rootPanel);

}

package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;

public abstract class ContentComposite extends Composite {
	private DockPanel rootPanel = new DockPanel();
	private boolean uiCreated;

	public ContentComposite() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName("sl-ContentComposite");
	}

	public abstract String getContextName();

	public final void show() {
		ClientContext.setContext(getContextName());
	}

	public final void activate() {
		if (!uiCreated) {
			uiCreated = true;
			onInitialize(rootPanel);
		}

		onActivate();
	}

	public final boolean deactivate() {
		return onDeactivate();
	}

	protected DockPanel getRootPanel() {
		return rootPanel;
	}

	protected abstract void onInitialize(DockPanel rootPanel);

	protected abstract void onActivate();

	protected abstract boolean onDeactivate();

}

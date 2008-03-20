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

	public abstract String getContentName();

	public final void show() {
		ClientContext.setContext(getContentName());
	}

	public final void activate(Context context) {
		if (!uiCreated) {
			uiCreated = true;
			onInitialize(rootPanel);
		}

		onActivate(context);
	}

	public final boolean deactivate() {
		return onDeactivate();
	}

	protected DockPanel getRootPanel() {
		return rootPanel;
	}

	protected abstract void onInitialize(DockPanel rootPanel);

	protected abstract void onActivate(Context context);

	protected abstract boolean onDeactivate();

}

package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;

public abstract class ContentComposite extends Composite {
	private final DockPanel rootPanel = new DockPanel();
	private boolean uiCreated;
	private boolean active;

	public ContentComposite() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName("sl-ContentComposite");
	}

	public final void show() {
		ClientContext.setContent(this);
	}

	public final void activate(Context context) {
		if (!uiCreated) {
			uiCreated = true;
			onInitialize(rootPanel);
		}

		active = true;
		onActivate(context);
	}

	public final void update(Context context) {
		if (active) {
			onUpdate(context);
		}
	}

	public final boolean deactivate() {
		active = false;
		return onDeactivate();
	}

	protected final DockPanel getRootPanel() {
		return rootPanel;
	}

	protected abstract void onInitialize(DockPanel rootPanel);

	protected abstract void onActivate(Context context);

	protected abstract void onUpdate(Context context);

	protected abstract boolean onDeactivate();

}

package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;

public abstract class ContentComposite extends Composite implements Lifecycle {
	private final DockPanel rootPanel = new DockPanel();
	private boolean initialized;
	private boolean active;

	public ContentComposite() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName("sl-ContentComposite");
	}

	public final void show() {
		ContextManager.setContent(this);
	}

	public final void update(Context context) {
		if (!initialized) {
			initialized = true;
			onInitialize();
		}

		onUpdate(context);
		active = true;
	}

	public final void deactivate() {
		active = false;
		onDeactivate();
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isActive() {
		return active;
	}

	protected final void onInitialize() {
		onInitialize(rootPanel);
	}

	protected final DockPanel getRootPanel() {
		return rootPanel;
	}

	protected abstract void onInitialize(DockPanel rootPanel);

	protected abstract void onUpdate(Context context);

	protected abstract void onDeactivate();
}

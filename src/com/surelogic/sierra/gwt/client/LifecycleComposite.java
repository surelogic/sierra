package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;

public abstract class LifecycleComposite extends Composite implements Lifecycle {
	private boolean initialized;
	private boolean active;

	public final void update(Context context) {
		if (!initialized) {
			initialized = true;
			onInitialize();
		}

		active = true;
		onUpdate(context);
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

	protected abstract void onInitialize();

	protected abstract void onUpdate(Context context);

	protected abstract void onDeactivate();

}

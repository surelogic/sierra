package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.Lifecycle;

public abstract class SectionPanel extends BasicPanel implements Lifecycle {
	private boolean initialized;
	private boolean active;

	public final void update(Context context) {
		if (!initialized) {
			initialized = true;
			onInitialize(getContentPanel());
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

	protected abstract void onInitialize(VerticalPanel contentPanel);

	protected abstract void onUpdate(Context context);

	protected abstract void onDeactivate();

}

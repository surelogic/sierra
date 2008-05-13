package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class BlockPanel extends BasicPanel {
	private boolean initialized;

	public final void initialize() {
		if (initialized) {
			return;
		}
		onInitialize(getContentPanel());
		initialized = true;
	}

	protected abstract void onInitialize(VerticalPanel contentPanel);

}

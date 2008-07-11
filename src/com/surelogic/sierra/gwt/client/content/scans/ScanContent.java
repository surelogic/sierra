package com.surelogic.sierra.gwt.client.content.scans;

import com.google.gwt.user.client.ui.DockPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;

public class ScanContent extends ContentComposite {

	@Override
	protected void onDeactivate() {
		// Do nothing
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel) {

	}

	@Override
	protected void onUpdate(final Context context) {
		final String uuid = context.getUuid();
	}

	private ScanContent() {
		// Do nothing
	}

	private static final ScanContent INSTANCE = new ScanContent();

	public static ScanContent getInstance() {
		return INSTANCE;
	}
}

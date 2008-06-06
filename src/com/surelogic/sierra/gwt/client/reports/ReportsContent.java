package com.surelogic.sierra.gwt.client.reports;

import com.google.gwt.user.client.ui.DockPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;

public class ReportsContent extends ContentComposite {
	private static final ReportsContent instance = new ReportsContent();

	public static ReportsContent getInstance() {
		return instance;
	}

	private ReportsContent() {
		super();
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDeactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onUpdate(Context context) {
		// TODO Auto-generated method stub

	}

}

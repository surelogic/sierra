package com.surelogic.sierra.client.eclipse.views.adhoc;

import org.eclipse.swt.widgets.Composite;

import com.surelogic.adhoc.views.menu.AbstractQueryMenuView;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.serviceability.UsageMeter;
import com.surelogic.sierra.client.eclipse.jobs.JobConstants;

public final class QueryMenuView extends AbstractQueryMenuView {
	public QueryMenuView() {
		super(JobConstants.ACCESS_KEY);
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		UsageMeter.getInstance().tickUse("Sierra QueryMenuView opened");
		super.createPartControl(parent);
	}

	@Override
	public AdHocManager getManager() {
		return AdHocDataSource.getManager();
	}
}

package com.surelogic.sierra.client.eclipse.views.adhoc;

import org.eclipse.swt.widgets.Composite;

import com.surelogic.adhoc.views.results.AbstractQueryResultsView;
import com.surelogic.common.serviceability.UsageMeter;

public final class QueryResultsView extends AbstractQueryResultsView {

	@Override
	public void createPartControl(final Composite parent) {
		UsageMeter.getInstance().tickUse("Sierra QueryResultsView opened");
		super.createPartControl(parent);
	}
}

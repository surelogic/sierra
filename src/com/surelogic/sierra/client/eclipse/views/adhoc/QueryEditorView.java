package com.surelogic.sierra.client.eclipse.views.adhoc;

import org.eclipse.swt.widgets.Composite;

import com.surelogic.adhoc.views.editor.AbstractQueryEditorView;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.serviceability.UsageMeter;
import com.surelogic.sierra.client.eclipse.jobs.JobConstants;

public final class QueryEditorView extends AbstractQueryEditorView {
	public QueryEditorView() {
		super(JobConstants.ACCESS_KEY);
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		UsageMeter.getInstance().tickUse("Sierra QueryEditorView opened");
		super.createPartControl(parent);
	}

	@Override
	public AdHocManager getManager() {
		return AdHocDataSource.getManager();
	}
}

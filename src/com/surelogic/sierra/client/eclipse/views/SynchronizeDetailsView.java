package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;

import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.sierra.jdbc.finding.SynchOverview;

public final class SynchronizeDetailsView extends
		AbstractSierraView<SynchronizeDetailsMediator> {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.SynchronizeDetailsView";

	@Override
	protected SynchronizeDetailsMediator createMorePartControls(Composite parent) {
		return new SynchronizeDetailsMediator(this);
	}

	public static void findingSelected(SynchOverview syncOverview,
			boolean moveFocus) {
		SynchronizeDetailsView view;
		if (moveFocus) {
			view = (SynchronizeDetailsView) ViewUtility.showView(ID);
		} else {
			view = (SynchronizeDetailsView) ViewUtility.showView(ID, null,
					IWorkbenchPage.VIEW_VISIBLE);
		}
		view.f_mediator.asyncQueryAndShow(syncOverview);
	}
}

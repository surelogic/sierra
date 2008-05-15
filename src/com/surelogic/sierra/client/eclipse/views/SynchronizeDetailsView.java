package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;

import com.surelogic.common.eclipse.LinkTrail;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.sierra.jdbc.finding.SynchOverview;

public final class SynchronizeDetailsView extends
		AbstractSierraView<SynchronizeDetailsMediator> {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.SynchronizeDetailsView";

	@Override
	protected SynchronizeDetailsMediator createMorePartControls(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		final Label eventInfo = new Label(panel, SWT.NONE);
		eventInfo
				.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		final LinkTrail detailsComposite = new LinkTrail(panel);
		detailsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		return new SynchronizeDetailsMediator(this, panel, eventInfo,
				detailsComposite);
	}

	/**
	 * Tells this view what synchronization event to show the details of.
	 * 
	 * @param syncOverview
	 *            the synchronization event to show the details of, or
	 *            {@code null} to clear the view.
	 * @param moveFocus
	 *            {@code true} to focus on this view}, {@code false} to leave
	 *            the focus as it is.
	 */
	public static void eventSelected(SynchOverview syncOverview,
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

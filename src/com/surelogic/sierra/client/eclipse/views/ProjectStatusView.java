package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public final class ProjectStatusView extends AbstractSierraView<ProjectStatusMediator> {
	public static final String ID = "com.surelogic.sierra.client.eclipse.views.ProjectStatusView";

	@Override
	protected ProjectStatusMediator createMorePartControls(Composite parent) {
		final Tree statusTree = new Tree(parent, SWT.NONE);
		
		/*
		final Action omitEmptyEntriesAction = 
			new Action("Omit Empty Entries", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				f_mediator.setHideEmptyEntries(isChecked());				
			}
		};
		addToViewMenu(omitEmptyEntriesAction);
		*/

		return new ProjectStatusMediator(this, statusTree);
	}
}

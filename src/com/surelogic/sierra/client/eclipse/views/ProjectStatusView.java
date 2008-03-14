package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.sierra.client.eclipse.actions.PreferencesAction;

public final class ProjectStatusView extends ViewPart {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.ProjectStatusView";

	private ProjectStatusMediator f_mediator = null;

	@Override
	public void dispose() {
		if (f_mediator != null)
			f_mediator.dispose();
		super.dispose();
	}

	@Override
	public void createPartControl(final Composite parent) {
		final Tree statusTree = new Tree(parent, SWT.NONE);

		/*
		 * Allow direct access to the preferences from the view.
		 */
		final IMenuManager menu = getViewSite().getActionBars()
				.getMenuManager();
		/*
		final Action omitEmptyEntriesAction = 
			new Action("Omit Empty Entries", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				f_mediator.setHideEmptyEntries(isChecked());				
			}
		};
		menu.add(omitEmptyEntriesAction);
		menu.add(new Separator());
		*/
		menu.add(new PreferencesAction("Preferences..."));
		
		/*
		 * Allow access to help via the F1 key.
		 */
		getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp(
				parent,
				"com.surelogic.sierra.client.eclipse.view-project-status");

		f_mediator = new ProjectStatusMediator(statusTree);
		f_mediator.init();
	}

	@Override
	public void setFocus() {
		if (f_mediator != null)
			f_mediator.setFocus();
	}
}

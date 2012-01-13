package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.sierra.client.eclipse.views.SierraServersView;

public final class ConfigureTeamServersAction implements
		IWorkbenchWindowActionDelegate {

	public void dispose() {
		// Nothing to do
	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do
	}

	public void run(IAction action) {
		EclipseUIUtility.showView(SierraServersView.ID);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}
}
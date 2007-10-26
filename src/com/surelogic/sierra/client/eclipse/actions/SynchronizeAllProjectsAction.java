package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public final class SynchronizeAllProjectsAction implements
		IWorkbenchWindowActionDelegate {

	public void dispose() {
		// Nothing to do
	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do
	}

	public void run(IAction action) {
		// TODO : sync all the projects open in the workspace and connected to
		// the server
		MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay()
				.getActiveShell(), "Unimplemented",
				"This functionality is not yet implemented.");
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}
}

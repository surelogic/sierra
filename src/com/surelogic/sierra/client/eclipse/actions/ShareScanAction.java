package com.surelogic.sierra.client.eclipse.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.dialogs.QualifierSelectionDialog;

public final class ShareScanAction implements IWorkbenchWindowActionDelegate {

	public void dispose() {
		// Nothing to do
	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do

	}

	public void run(IAction action) {
		Set<String> q = new HashSet<String>();
		q.add("qual 1");
		q.add("qual 2");
		q.add("qual 3");
		q.add("qual 4");
		QualifierSelectionDialog dialog = new QualifierSelectionDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				q, "project1", "server1");
		dialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}

}

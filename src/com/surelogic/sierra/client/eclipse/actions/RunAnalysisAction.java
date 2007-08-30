package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class RunAnalysisAction implements IObjectActionDelegate,
		IWorkbenchWindowActionDelegate {

	private IStructuredSelection currentSelection = null;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Nothing to do
	}

	public void run(IAction action) {
		if (currentSelection != null) {
			RunAnalysis ra = new RunAnalysis(currentSelection);
			ra.execute();
		}
		// } else {
		// SLLogger.getLogger().log(Level.WARNING,
		// "Invalid selection for running analysis", new Exception());
		//		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			currentSelection = (IStructuredSelection) selection;
		} 
		else {
			currentSelection = null;
			// SLLogger.getLogger()
			// .log(Level.WARNING,
			// "Selection is not an IStructuredSelection",
			// new Exception());
		}
	}

	public void dispose() {
		// Nothing to do
	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do
	}
}

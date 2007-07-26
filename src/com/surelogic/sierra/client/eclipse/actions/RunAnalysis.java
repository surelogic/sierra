package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.surelogic.sierra.client.eclipse.SLog;

public final class RunAnalysis implements IObjectActionDelegate {

	private IStructuredSelection f_selection = null;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
	}

	public void run(IAction action) {
		List<IJavaProject> selectedProjects = new ArrayList<IJavaProject>();
		if (f_selection != null) {
			for (Object selection : f_selection.toArray()) {
				if (selection instanceof IJavaProject) {
					selectedProjects.add((IJavaProject) selection);
				}
			}
			for (IJavaProject project : selectedProjects) {
				System.out.println("Run on " + project.getProject().getName());
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			action.setEnabled(true);
			f_selection = (IStructuredSelection) selection;
		} else {
			f_selection = null;
			action.setEnabled(false);
			SLog.logWarning("Selection is not an IStructuredSelection",
					new Exception());
		}
	}
}

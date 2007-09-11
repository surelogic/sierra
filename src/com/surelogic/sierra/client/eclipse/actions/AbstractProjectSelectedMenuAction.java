package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This class allows both a main menu action and a right-click context menu on
 * Java projects.
 */
public abstract class AbstractProjectSelectedMenuAction implements
		IObjectActionDelegate, IWorkbenchWindowActionDelegate {

	protected abstract void run(List<IJavaProject> selectedProjects,
			List<String> projectNames);

	private IStructuredSelection f_currentSelection = null;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Nothing to do
	}

	public final void run(IAction action) {
		if (f_currentSelection != null) {
			final List<IJavaProject> selectedProjects = new ArrayList<IJavaProject>();
			final List<String> selectedProjectNames = new ArrayList<String>();
			for (Object selection : f_currentSelection.toArray()) {
				if (selection instanceof IJavaProject) {
					final IJavaProject javaProject = (IJavaProject) selection;
					selectedProjects.add(javaProject);
					selectedProjectNames.add(javaProject.getElementName());
				}
			}
			run(selectedProjects, selectedProjectNames);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			f_currentSelection = (IStructuredSelection) selection;
		} else {
			f_currentSelection = null;
		}
	}

	public void dispose() {
		// Nothing to do
	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do
	}
}

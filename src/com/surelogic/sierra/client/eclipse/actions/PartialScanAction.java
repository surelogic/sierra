package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public class PartialScanAction implements IWorkbenchWindowActionDelegate {
	private IStructuredSelection f_currentSelection = null;

	public void dispose() {
		// Nothing for now

	}

	public void init(IWorkbenchWindow window) {
		// Nothing for now

	}

	public void run(IAction action) {
		if (f_currentSelection != null) {
			final List<ICompilationUnit> selectedCompilationUnits = new ArrayList<ICompilationUnit>();
			for (Object selection : f_currentSelection.toArray()) {
				if (selection instanceof ICompilationUnit) {
					final ICompilationUnit compilationUnit = (ICompilationUnit) selection;
					selectedCompilationUnits.add(compilationUnit);
				}
			}

			boolean saveCancelled = true;
			// Bug 1075 Fix - Ask for saving editors
			if (!PreferenceConstants.alwaysSaveResources()) {
				saveCancelled = PlatformUI.getWorkbench().saveAllEditors(true);
			} else {
				PlatformUI.getWorkbench().saveAllEditors(false);
			}
			if (saveCancelled) {
				new Scan()
						.executeScanForCompilationUnits(selectedCompilationUnits);
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			f_currentSelection = (IStructuredSelection) selection;
		} else {
			f_currentSelection = null;
		}

	}

}

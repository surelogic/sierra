package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.logging.SLLogger;
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
			final List<IPackageFragment> selectedPackageFragments = new ArrayList<IPackageFragment>();
			for (Object selection : f_currentSelection.toArray()) {
				if (selection instanceof ICompilationUnit) {
					final ICompilationUnit compilationUnit = (ICompilationUnit) selection;
					selectedCompilationUnits.add(compilationUnit);
				}

				if (selection instanceof IPackageFragment) {
					final IPackageFragment packageFragment = (IPackageFragment) selection;
					selectedPackageFragments.add(packageFragment);
				}
			}

			if (selectedPackageFragments.size() > 0) {
				for (IPackageFragment packageFragment : selectedPackageFragments) {
					try {
						for (ICompilationUnit compilationUnit : packageFragment
								.getCompilationUnits()) {
							selectedCompilationUnits.add(compilationUnit);
						}

					} catch (JavaModelException e) {
						SLLogger.getLogger("sierra").log(
								Level.SEVERE,
								"Error when trying to get compilation units for package "
										+ packageFragment.getElementName(), e);
					}

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

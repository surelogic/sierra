package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.dialogs.InstallTutorialProjectsDialog;

public class ImportTutorialProjectsAction implements
		IWorkbenchWindowActionDelegate {

	public void dispose() {
		// Do nothing
	}

	public void init(final IWorkbenchWindow window) {
		// Do nothing
	}

	public void run(final IAction action) {
		InstallTutorialProjectsDialog.open(
				EclipseUIUtility.getShell(),
				CommonImages.IMG_SIERRA_LOGO,
				"/com.surelogic.sierra.client.eclipse.help/ch01s04.html",
				Thread.currentThread().getContextClassLoader()
						.getResource("/resources/ShowOff.zip"),
				Thread.currentThread().getContextClassLoader()
						.getResource("/resources/SmallWorld.zip"));
	}

	public void selectionChanged(final IAction action,
			final ISelection selection) {
		// Do nothing
	}

}
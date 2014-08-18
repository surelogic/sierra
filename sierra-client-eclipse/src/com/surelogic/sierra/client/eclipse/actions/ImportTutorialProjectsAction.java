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

	@Override
  public void dispose() {
		// Do nothing
	}

	@Override
  public void init(final IWorkbenchWindow window) {
		// Do nothing
	}

	@Override
  public void run(final IAction action) {
		InstallTutorialProjectsDialog.open(
				EclipseUIUtility.getShell(),
				CommonImages.IMG_SIERRA_LOGO,
				"/com.surelogic.sierra.client.eclipse.help/ch01s04.html",
				Thread.currentThread().getContextClassLoader()
						.getResource("/lib/SierraTutorial_ShowOff.zip"),
				Thread.currentThread().getContextClassLoader()
						.getResource("/lib/SierraTutorial_SmallWorld.zip"));
	}

	@Override
  public void selectionChanged(final IAction action,
			final ISelection selection) {
		// Do nothing
	}

}

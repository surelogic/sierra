package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.dialogs.UserPasswordDialog;

public final class DisconnectAction extends AbstractProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects) {
		// TODO: Just for testing of the user password dialog
		UserPasswordDialog d = new UserPasswordDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), "Fluid", "http",
				"fluid.surelogic.com", 8080, null);
		d.open();

	}
}

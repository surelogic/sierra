package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.model.SierraServerModel;

public final class DisconnectAction extends AbstractProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects) {
		SierraServerModel s = new SierraServerModel(new SierraServerManager(
				new File("foo")), "Server 1");
		s.setHost("fluid.surelogic.com");
		// TODO: Just for testing of the user password dialog
		ServerAuthenticationDialog d = new ServerAuthenticationDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				s);
		d.open();

	}
}

package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.model.SierraServerModel;

public final class DisconnectAction extends AbstractProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects) {
		SierraServerManager m = new SierraServerManager();
		SierraServerModel s = new SierraServerModel(m, "Server 1");
		SierraServerModel s2 = new SierraServerModel(m, "Server 2");
		s.setHost("fluid.surelogic.com");
		// TODO: Just for testing of the user password dialog
		ServerLocationDialog d = new ServerLocationDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell(), s, true);
		d.open();
		System.out.println(s);

	}
}

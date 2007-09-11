package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public final class DisconnectAction extends AbstractProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects,
			List<String> projectNames) {

		if (projectNames.size() == 0)
			return;

		final Shell shell = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell();
		final boolean multiDisconnect = projectNames.size() > 1;

		final MessageBox confirmDisconnect = new MessageBox(shell, SWT.ICON_WARNING
				| SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
		confirmDisconnect.setText("Confirm "
				+ (multiDisconnect ? "Multiple Project" : "Project")
				+ " Disconnect");
		confirmDisconnect
				.setMessage("Are you sure you want to disconnect "
						+ (multiDisconnect ? "these " + projectNames.size()
								+ " projects" : "the project '"
								+ projectNames.get(0) + "'")
						+ " from "
						+ (multiDisconnect ? "their" : "its")
						+ " Sierra server?  This action will delete stored Sierra data about "
						+ (multiDisconnect ? "these projects" : "this project")
						+ " from your Eclipse workspace.  This action will not "
						+ "change or delete any data on any Sierra server.");
		if (confirmDisconnect.open() == SWT.NO)
			return; // bail
		System.out.println("disconnect run");
	}
}

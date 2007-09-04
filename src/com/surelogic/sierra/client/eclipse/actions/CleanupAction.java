package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.sierra.client.eclipse.model.Projects;

public final class CleanupAction extends AbstractProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects) {
		if (selectedProjects.size() == 1) {

			String holder = selectedProjects.get(0).getElementName();

			if (Projects.getInstance().exists(holder)) {
				boolean isConfirm = MessageDialog.openConfirm(new Shell(),
						"Confirm Project Cleanup",
						"Are you sure you want to delete all the Sierra data for '"
								+ holder + "'?");
				if (isConfirm) {
					System.out.println("Deleting " + holder);
					// Delete the project
				}
			} else {
				MessageDialog.openError(new Shell(), "Sierra", "Project '"
						+ holder + "' does not exist in the database.");
			}
		} else if (selectedProjects.size() > 1) {

			boolean projectExists = false;
			Iterator<IJavaProject> projectIterator = selectedProjects
					.iterator();

			List<IJavaProject> updatedSelectedProjects = new ArrayList<IJavaProject>();
			while (projectIterator.hasNext()) {
				IJavaProject currentProject = projectIterator.next();
				boolean currentProjectExists = Projects.getInstance().exists(
						currentProject.getElementName());
				if (currentProjectExists) {
					updatedSelectedProjects.add(currentProject);
				}
				projectExists = projectExists || currentProjectExists;
			}

			if (projectExists) {
				boolean isConfirm = MessageDialog.openConfirm(new Shell(),
						"Confirm Multiple Project Cleanup",
						"Are you sure you want to delete all the Sierra data for these "
								+ selectedProjects.size() + " projects?");
				if (isConfirm) {
					Iterator<IJavaProject> updatedSelectedProjectsIterator = updatedSelectedProjects
							.iterator();
					while (updatedSelectedProjectsIterator.hasNext()) {
						IJavaProject holder = updatedSelectedProjectsIterator
								.next();
						System.out.println("Deleting "
								+ holder.getElementName());
						// Delete the project data
					}
				}
			} else {
				MessageDialog.openError(new Shell(), "Sierra",
						"Projects do not exist in the database.");
			}
		}
	}
}

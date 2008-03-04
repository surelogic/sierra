package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.dialogs.JavaProjectSelectionDialog;

public class SynchronizeProjectDialogAction extends SynchronizeProjectAction {
  @Override
  protected void run(List<IJavaProject> selectedProjects,
      List<String> projectNames) {
    final List<IJavaProject> projects = JavaProjectSelectionDialog
    .getProjects("Select project(s) to synchronize:", "Synchronize Project",
        SLImages.getImage(SLImages.IMG_SIERRA_SYNC),
        selectedProjects);
    if (selectedProjects == projects) {
      super.run(selectedProjects, projectNames);
    } else {
      super.run(projects, getNames(projects));
    }
  }
}

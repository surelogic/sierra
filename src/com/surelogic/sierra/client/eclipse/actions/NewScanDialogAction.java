package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.sierra.client.eclipse.dialogs.JavaProjectSelectionDialog;

public class NewScanDialogAction extends NewScanAction {
  @Override
  protected void run(final List<IJavaProject> selectedProjects,
      final List<String> projectNames) {
    final List<IJavaProject> projects = 
      JavaProjectSelectionDialog.getProjects("Select Projects to Scan", selectedProjects);
    if (selectedProjects == projects) {
      super.run(selectedProjects, projectNames);
    } else {
      super.run(projects, null);
    }
  }
}

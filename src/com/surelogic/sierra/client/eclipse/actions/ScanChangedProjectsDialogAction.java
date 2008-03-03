package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.sierra.client.eclipse.dialogs.JavaProjectSelectionDialog;

public class ScanChangedProjectsDialogAction extends ScanChangedProjectsAction {
  @Override
  public void run(List<IJavaProject> projects) {
    projects = 
      JavaProjectSelectionDialog.getProjects("Select Projects to Re-Scan", projects);
    super.run(projects);
  }
}

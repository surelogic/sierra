package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.dialogs.JavaProjectSelectionDialog;

public class PublishScanDialogAction extends PublishScanAction {
  @Override
  protected void run(List<IJavaProject> selectedProjects,
      List<String> projectNames) {
    final List<IJavaProject> projects = JavaProjectSelectionDialog
    .getProjects("Select project(s) to publish:", "Publish Scan for Project",
        SLImages.getImage(CommonImages.IMG_SIERRA_PUBLISH),
        selectedProjects);
    if (selectedProjects == projects) {
      super.run(selectedProjects, projectNames);
    } else {
      super.run(projects, getNames(projects));
    }
  }
}

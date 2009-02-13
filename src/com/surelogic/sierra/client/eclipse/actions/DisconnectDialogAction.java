package com.surelogic.sierra.client.eclipse.actions;

import java.util.*;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.dialogs.JavaProjectSelectionDialog;

public class DisconnectDialogAction extends DisconnectAction {
  @Override
  protected void run(List<IJavaProject> selectedProjects,
      List<String> projectNames) {
    final List<IJavaProject> projects = JavaProjectSelectionDialog
    .getProjects("Select project(s) to disconnect:", "Disconnect Project",
        SLImages.getImage(CommonImages.IMG_SIERRA_DISCONNECT),
        selectedProjects);
    if (selectedProjects == projects) {
      super.run(selectedProjects, projectNames);    
    } else {
      List<String> names = getNames(projects);
      super.run(projects, names);
    }
  }
}

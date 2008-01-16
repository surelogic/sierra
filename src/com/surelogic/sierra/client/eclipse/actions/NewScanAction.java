package com.surelogic.sierra.client.eclipse.actions;

import java.util.*;

import org.eclipse.jdt.core.*;

/**
 * For testing the new code
 * @author Edwin.Chan
 */
public class NewScanAction extends AbstractProjectSelectedMenuAction {
  @Override
  protected void run(final List<IJavaProject> selectedProjects,
      final List<String> projectNames) {
    NewScan.scan(selectedProjects, projectNames);
  }
}

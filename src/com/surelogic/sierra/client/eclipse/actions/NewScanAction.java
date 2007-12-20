package com.surelogic.sierra.client.eclipse.actions;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.findbugs.FindBugs1_3_0Tool;
import com.surelogic.sierra.tool.pmd.PMD4_0Tool;
import com.surelogic.sierra.tool.targets.*;

/**
 * For testing the new code
 * @author Edwin.Chan
 */
public class NewScanAction extends AbstractProjectSelectedMenuAction {
  /** The logger */
  private static final Logger LOG = SLLogger.getLogger("sierra");
  
  @Override
  protected void run(final List<IJavaProject> selectedProjects,
      List<String> projectNames) {
    boolean saved = true;
    // Bug 1075 Fix - Ask for saving editors
    if (!PreferenceConstants.alwaysSaveResources()) {
      saved = PlatformUI.getWorkbench().saveAllEditors(true);
    } else {
      PlatformUI.getWorkbench().saveAllEditors(false);
    }
    if (saved) {
      new Job("New Scan") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(monitor);
          try {
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            ITool t = true ? new FindBugs1_3_0Tool() : new PMD4_0Tool();
            IToolInstance ti = t.create(wrapper);  
            
            for(IJavaProject p : selectedProjects) {
              for(IClasspathEntry cpe : p.getResolvedClasspath(true)) {
                if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                  IResource res = root.findMember(cpe.getPath());
                  URI loc = res.getLocationURI();
                  ti.addTarget(new DirectoryTarget(true, loc) {
                    public boolean exclude(String relativePath) {
                      // TODO Auto-generated method stub
                      return false;
                    }
                  });
                }
              }
              URI out = root.findMember(p.getOutputLocation()).getLocationURI();
              ti.addTarget(new FullDirectoryTarget(false, out));
            }
            ti.run();
          } catch(Throwable ex) {
            wrapper.failed(ex);
          }
          if (wrapper.getFailureTrace() != null) {
            Throwable ex = wrapper.getFailureTrace();
            LOG.severe(ex.getMessage());
            return SLStatus.createErrorStatus("New Scan failed", ex);
          }
          return Status.OK_STATUS;
        }
      }.schedule();
    }
  }
}

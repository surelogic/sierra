package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

import org.eclipse.core.resources.IResource;
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
import com.surelogic.sierra.tool.pmd40.PMDTool;
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
            ITool t = new PMDTool();
            IToolInstance ti = t.create(wrapper);  
            for(IJavaProject p : selectedProjects) {
              for(IClasspathEntry cpe : p.getResolvedClasspath(true)) {
                if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                  IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(cpe.getPath());
                  URI loc = res.getLocationURI();
                  ti.addTarget(new DirectoryTarget(true, loc) {
                    public boolean exclude(String relativePath) {
                      // TODO Auto-generated method stub
                      return false;
                    }
                    public Iterable<URI> getFiles() {
                      List<URI> files = new ArrayList<URI>();
                      findFiles(files, new File(getLocation()));
                      return files;
                    }            
                    private void findFiles(List<URI> files, File here) {   
                      if (!here.exists()) {
                        return;
                      }
                      if (here.isDirectory()) {
                        for(File f : here.listFiles()) {
                          findFiles(files, f);
                        }
                      }
                      else if (here.isFile() && here.getName().endsWith(".java")) {
                        files.add(here.toURI());
                      }
                    }
                  });
                }
              }
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

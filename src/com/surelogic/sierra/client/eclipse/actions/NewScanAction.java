package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.*;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.ConfigGenerator;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.MessageArtifactFileGenerator;
import com.surelogic.sierra.tool.findbugs.FindBugs1_3_0Tool;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.pmd.PMD4_0Tool;
import com.surelogic.sierra.tool.reckoner.Reckoner1_0Tool;
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
            ITool t = true ? new FindBugs1_3_0Tool() : new PMD4_0Tool();
            if (true) {
              t = new Reckoner1_0Tool();
            }
            for(IJavaProject p : selectedProjects) {
              Config config = ConfigGenerator.getInstance().getProjectConfig(p);
              // FIX this
              File tempDocument = File.createTempFile("sierra-"+p.getElementName(), ".scan.xml");
              ArtifactGenerator generator = 
                new MessageArtifactFileGenerator(tempDocument, config);
              
              IToolInstance ti = t.create(generator, wrapper);                         
              setupToolForProject(ti, p, true);
              ti.run();
            }            
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

        /**
         * @param toBeAnalyzed Whether the project will be analyzed, or is simply referred to
         */
        private void setupToolForProject(final IToolInstance ti, IJavaProject p, final boolean toBeAnalyzed) 
        throws JavaModelException 
        {
          final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
          for(IClasspathEntry cpe : p.getResolvedClasspath(true)) {
            switch (cpe.getEntryKind()) {
              case IClasspathEntry.CPE_SOURCE:
                if (toBeAnalyzed) {
                  IResource res = root.findMember(cpe.getPath());
                  URI loc = res.getLocationURI();
                  ti.addTarget(new DirectoryTarget(IToolTarget.Type.SOURCE, loc) {
                    public boolean exclude(String relativePath) {
                      // TODO Auto-generated method stub
                      return false;
                    }
                  });
                }
                break;
              case IClasspathEntry.CPE_LIBRARY:
                URI lib;
                IPath libPath = cpe.getPath();
                File libFile = new File(libPath.toOSString());
                if (libFile.exists()) {
                  lib = libFile.toURI();
                } else {
                  lib = root.findMember(libPath).getLocationURI();
                }
                if (new File(lib).isDirectory()) {
                  ti.addTarget(new FullDirectoryTarget(IToolTarget.Type.AUX, lib));
                } else {
                  ti.addTarget(new JarTarget(lib));
                }
                break;
              case IClasspathEntry.CPE_PROJECT:
                String projName = cpe.getPath().lastSegment();
                IProject proj = root.getProject(projName);
                setupToolForProject(ti, JavaCore.create(proj), false);
                break;
            }
          }
          URI out = root.findMember(p.getOutputLocation()).getLocationURI();
          ti.addTarget(new FullDirectoryTarget(toBeAnalyzed ? IToolTarget.Type.BINARY : IToolTarget.Type.AUX, out));
        }
      }.schedule();
    }
  }
}

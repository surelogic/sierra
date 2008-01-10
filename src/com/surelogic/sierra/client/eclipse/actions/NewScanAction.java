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
import com.surelogic.sierra.client.eclipse.jobs.ScanDocumentUtility;
import com.surelogic.sierra.client.eclipse.model.ConfigGenerator;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
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
            for(IJavaProject p : selectedProjects) {
              final Config config = ConfigGenerator.getInstance().getProjectConfig(p);
              System.out.println("Excluded: "+config.getExcludedToolsList());

              final MultiTool t = new MultiTool();
              if (!config.getExcludedToolsList().contains("findbugs")) {
                t.addTool(new FindBugs1_3_0Tool());
              }
              if (!config.getExcludedToolsList().contains("pmd")) {
                t.addTool(new PMD4_0Tool());
              }
              if (!config.getExcludedToolsList().contains("reckoner")) {
                t.addTool(new Reckoner1_0Tool());
              }
              System.out.println("Java version: "+config.getJavaVersion());
              System.out.println("Rules file: "+config.getPmdRulesFile());
              // FIX this
              // File tempDocument = File.createTempFile("sierra-"+p.getElementName(), ".scan.gz");
              ArtifactGenerator generator = 
                new MessageArtifactFileGenerator(config.getScanDocument(), config);
              
              IToolInstance ti = t.create(generator, wrapper);                         
              setupToolForProject(ti, p, true);
              ti.run();
              generator.finished();
              
              ScanDocumentUtility.loadScanDocument(config.getScanDocument(), wrapper,
                                                   config.getProject());
              /* Notify that scan was completed */
              DatabaseHub.getInstance().notifyScanLoaded();
            }            
          } catch(Throwable ex) {
            wrapper.failed("Caught exception during run()", ex);
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
            handleClasspathEntry(ti, toBeAnalyzed, root, cpe);
          }
          URI out = root.findMember(p.getOutputLocation()).getLocationURI();
          ti.addTarget(new FullDirectoryTarget(toBeAnalyzed ? IToolTarget.Type.BINARY : IToolTarget.Type.AUX, out));
        }

        private void handleClasspathEntry(final IToolInstance ti, final boolean toBeAnalyzed, 
                                          final IWorkspaceRoot root, IClasspathEntry cpe) 
        throws JavaModelException 
        {
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
              IPath srcPath = cpe.getSourceAttachmentPath();
              // FIX cpe.getSourceAttachmentRootPath();
              if (srcPath != null) {
                IToolTarget srcTarget = createTarget(root, cpe.getSourceAttachmentPath(), null);
                ti.addTarget(createTarget(root, cpe.getPath(), srcTarget));
              } else {
                ti.addTarget(createTarget(root, cpe.getPath(), null));
              }
              break;
            case IClasspathEntry.CPE_PROJECT:
              String projName = cpe.getPath().lastSegment();
              IProject proj = root.getProject(projName);
              setupToolForProject(ti, JavaCore.create(proj), false);
              break;
          }
        }

        private IToolTarget createTarget(final IWorkspaceRoot root, IPath libPath, IToolTarget src) {
          URI lib;
          File libFile = new File(libPath.toOSString());
          if (libFile.exists()) {
            lib = libFile.toURI();
          } else {
            lib = root.findMember(libPath).getLocationURI();
          }
          if (new File(lib).isDirectory()) {
            return new FullDirectoryTarget(IToolTarget.Type.AUX, lib);
          } else {
            return new JarTarget(lib);
          }
        }
      }.schedule();
    }
  }
}

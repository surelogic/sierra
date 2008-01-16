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
import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.jobs.*;
import com.surelogic.sierra.client.eclipse.model.ConfigGenerator;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.*;
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
    if (selectedProjects.size() <= 0) {
      return;
    }
    boolean saved = true;
    // Bug 1075 Fix - Ask for saving editors
    if (!PreferenceConstants.alwaysSaveResources()) {
      saved = PlatformUI.getWorkbench().saveAllEditors(true);
    } else {
      PlatformUI.getWorkbench().saveAllEditors(false);
    }
    if (saved) {
      final StringBuilder sb = new StringBuilder("Scanning ");
      /*
       * Fix for bug 1157. At JPL we encountered 87 projects and
       * the balloon pop-up went off the screen.
       */
      if (projectNames.size() <= 5) {
        boolean first = true;
        for(String name : projectNames) {
          if (first == true) {
            first = false;
          } else {
            sb.append(", ");
          }
          sb.append(name);          
        }
        sb.append(". ");
      } else {
        sb.append(projectNames.size()).append(" projects. ");
      }
      
      for(final IJavaProject p : selectedProjects) {
        Job job = new Job("Running Sierra on " + p.getElementName()) {
          @Override
          protected IStatus run(IProgressMonitor monitor) {
            final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(monitor);
            try {            
              final Config config = ConfigGenerator.getInstance().getProjectConfig(p);
              System.out.println("Excluded: "+config.getExcludedToolsList());

              final ITool t = ToolUtil.create(config);                           
              System.out.println("Java version: "+config.getJavaVersion());
              System.out.println("Rules file: "+config.getPmdRulesFile());

              IToolInstance ti = t.create(config, wrapper);                         
              setupToolForProject(ti, p, true);
              ti.run();

              final Runnable runAfter = new Runnable() {
                public void run() {
                  /* Notify that scan was completed */
                  DatabaseHub.getInstance().notifyScanLoaded();  
                  
                  /* Rename the scan document */
                  File scanDocument = config.getScanDocument();
                  File newScanDocument = null;

                  newScanDocument = new File(PreferenceConstants
                      .getSierraPath()
                      + File.separator
                      + config.getProject()
                      + SierraToolConstants.PARSED_FILE_SUFFIX);
                  /*
                   * This approach assures that the scan document
                   * generation will not crash. The tool will simply
                   * override the existing scan document no matter how
                   * recent it is.
                   */
                  if (newScanDocument.exists()) {
                    newScanDocument.delete();
                  }
                  scanDocument.renameTo(newScanDocument);
                }
              };
              Job importJob = new ImportScanDocumentJob(config.getScanDocument(), config.getProject(), runAfter);
              importJob.addJobChangeListener(new ScanProjectJobAdapter(config.getProject()));
              importJob.schedule();
            } catch(Throwable ex) {
              if (monitor.isCanceled()) {
                wrapper.failed("Caught exception during run()", ex);
              }
            }
            if (wrapper.getFailureTrace() != null && !monitor.isCanceled()) {
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

                  IPath[] includePatterns = cpe.getInclusionPatterns();                
                  IPath[] excludePatterns = cpe.getExclusionPatterns();
                  if ((excludePatterns != null && excludePatterns.length > 0) || 
                      (includePatterns != null && includePatterns.length > 0)) {
                    final String[] inclusions = convertPaths(includePatterns);
                    final String[] exclusions = convertPaths(excludePatterns);                
                    ti.addTarget(new FilteredDirectoryTarget(IToolTarget.Type.SOURCE, loc,
                        inclusions, exclusions));
                  } else {
                    ti.addTarget(new FullDirectoryTarget(IToolTarget.Type.SOURCE, loc));
                  }
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

          private String[] convertPaths(IPath[] patterns) {
            if (patterns == null || patterns.length == 0) {
              return null;
            }
            final String[] exclusions = new String[patterns.length];
            int i = 0;
            for(IPath exclusion : patterns) {
              exclusions[i] = exclusion.toString();
              i++;
            }
            return exclusions;
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
        };
        job.schedule();
      }

      if (PreferenceConstants.showBalloonNotifications()) {
        sb.append("You may continue your work. ");
        sb.append("You will be notified when the");
        sb.append(" scan has completed.");
        BalloonUtility.showMessage("Sierra Scan Started", sb
            .toString());
      }
    }
  }
}

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

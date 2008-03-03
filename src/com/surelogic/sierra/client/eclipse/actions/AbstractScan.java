package com.surelogic.sierra.client.eclipse.actions;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.XUtil;
import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.eclipse.jdt.JavaUtil;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.dialogs.*;
import com.surelogic.sierra.client.eclipse.model.ConfigCompilationUnit;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.message.Config;

public abstract class AbstractScan<T extends IJavaElement>  {
  /** The logger */
  protected static final Logger LOG = SLLogger.getLogger();

  private final boolean isRescan;
  
  protected AbstractScan(boolean isRescan) {
    this.isRescan = isRescan;
  }
  
  /**   
   * @return if editors were saved   
   */
  protected boolean trySavingEditors() {
    boolean saved;

    // Bug 1075 Fix - Ask for saving editors
    if (!PreferenceConstants.alwaysSaveResources()) {
      saved = PlatformUI.getWorkbench().saveAllEditors(true);
    } else {
      saved = PlatformUI.getWorkbench().saveAllEditors(false);
    }
    return saved;
  }

  protected StringBuilder computeLabel(final List<String> names) {
    final StringBuilder sb = new StringBuilder(isRescan ? "Re-scanning " : "Scanning ");
    /*
     * Fix for bug 1157. At JPL we encountered 87 projects and
     * the balloon pop-up went off the screen.
     */
    if (names.size() <= 5) {
      boolean first = true;
      for(String name : names) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(name);          
      }
    } else {
      sb.append(names.size());
      sb.append(isRescan ? " comp units" : " projects");
    }
    return sb;
  }

  public void scan(final Collection<T> elements) {
    List<String> names = new ArrayList<String>(elements.size());
    for(T elt : elements) {
      names.add(elt.getElementName());
    }
    scan(elements, names);
  }
  
  protected void scan(final Collection<T> elements, final List<String> names) {
    if (elements.size() <= 0) {
      return;
    }
    new UIJob(PlatformUI.getWorkbench().getDisplay(), "Checking if editors need to be saved") {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        final boolean saved = trySavingEditors();
        new WorkspaceJob("Checking if source code is built and compiles") {
          @Override
          public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
            try {
              boolean built    = checkIfBuilt(elements);
              boolean compiled = JavaUtil.noCompilationErrors(elements);

              if (saved & built & compiled) {
                final StringBuilder sb = computeLabel(names); // FIX merge w/ showStartBalloon?
                if (startScanJob(elements)) {      
                  showStartBalloon(sb);
                }
              } else if (!built) {
                BalloonUtility.showMessage("Something isn't built", 
                "Sierra cannot run properly if your code isn't fully compiled");
              } else if (!compiled) {
                BalloonUtility.showMessage("Something doesn't compile", 
                "Sierra cannot run properly if your code does not compile");
              } else {
                // Scan not run, because of modified editors   
                BalloonUtility.showMessage("Modified editors", 
                "Sierra did not run a scan due to unsaved editors");
              }
            } catch(CoreException e) {
              //BalloonUtility.showMessage("Problem while checking if your code compiles", );
              LOG.log(Level.SEVERE, "Problem while checking if your code compiles", e);
            }
            return Status.OK_STATUS;
          }          
        }.schedule();

        return Status.OK_STATUS;
      }
    }.schedule();
  } 

  protected void showStartBalloon(final StringBuilder label) {
    if (PreferenceConstants.showBalloonNotifications()) {
      label.append(". ");
      label.append("You may continue your work. ");
      label.append("You will be notified when the");
      if (!isRescan) {
        label.append(" scan has completed.");
        BalloonUtility.showMessage("Sierra scan started", label
            .toString());
      } else {
        label.append(" re-scan has completed.");
        BalloonUtility.showMessage("Sierra re-scan started", label
            .toString());
      }
    }
  }
  
  protected final boolean setupConfigs(final List<Config> configs) {
    if (configs.isEmpty()) {
      return false;
    }
    if (!XUtil.useExperimental()) {
      return true;
    }
    UIJob job = new UIJob("Put up test dialog") {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        final Shell shell = 
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        final ScanTestCodeSelectionDialog dialog = 
          new ScanTestCodeSelectionDialog(shell, configs);
        
        if (dialog.open() == Window.CANCEL) {
          return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
      }
    };
    job.setSystem(true);
    job.schedule();
    try {
      job.join();
    } catch (InterruptedException e) {
      // Nothing to do
    }    
    return job.getResult() != Status.CANCEL_STATUS;
  }
  
  protected final boolean setupCUConfigs(List<ConfigCompilationUnit> cuConfigs) {
    if (!XUtil.useExperimental()) {
      return true;
    }
    List<Config> configs = new ArrayList<Config>();  
    for(final ConfigCompilationUnit config : cuConfigs) {
      configs.add(config.getConfig());
    }
    return setupConfigs(configs);
  }
  
  abstract boolean checkIfBuilt(Collection<T> elements);
  
  /**
   * @return true if we started one or more scan jobs
   */
  abstract boolean startScanJob(Collection<T> elements);
}

package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.ResourcesPlugin;
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
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.BalloonUtility;
import com.surelogic.sierra.client.eclipse.dialogs.ScanTestCodeSelectionDialog;
import com.surelogic.sierra.client.eclipse.model.ConfigCompilationUnit;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.tool.message.Config;

public abstract class AbstractScan<T extends IJavaElement> {
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
    if (!EclipseUtility.getBooleanPreference(SierraPreferencesUtility.ALWAYS_SAVE_RESOURCES)) {
      saved = PlatformUI.getWorkbench().saveAllEditors(true);
    } else {
      saved = PlatformUI.getWorkbench().saveAllEditors(false);
    }
    return saved;
  }

  protected StringBuilder computeLabel(final Collection<String> names) {
    final StringBuilder sb = new StringBuilder(isRescan ? "Re-scanning " : "Scanning ");
    /*
     * Fix for bug 1157. At JPL we encountered 87 projects and the balloon
     * pop-up went off the screen.
     */
    if (names.size() <= 5) {
      boolean first = true;
      for (String name : names) {
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

  public void scan(final T elt) {
    List<T> elts = new ArrayList<>(1);
    elts.add(elt);
    scan(elts);
  }

  public void scan(final Collection<T> elements) {
    if (elements == null || elements.isEmpty()) {
      return;
    }

    final List<String> names = new ArrayList<>(elements.size());
    for (T elt : elements) {
      names.add(elt.getElementName());
    }

    new UIJob(PlatformUI.getWorkbench().getDisplay(), "Checking if editors need to be saved") {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        final boolean saved = trySavingEditors();
        final WorkspaceJob job = new WorkspaceJob("Checking if source code is built and compiles") {
          @Override
          public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
            try {
              boolean built = checkIfBuilt(elements);
              Collection<String> erroneous = JDTUtility.findCompilationErrors(elements, monitor);
              boolean compiled = erroneous.isEmpty();
              if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
              }

              if (saved & built & compiled) {
                final StringBuilder sb = computeLabel(names);
                // TODO ^ merge w/ showStartBalloon?
                if (startScanJob(elements)) {
                  showStartBalloon(sb);
                }
              } else if (!built) {
                BalloonUtility.showMessage("Something isn't compiled", "Sierra cannot scan if your code isn't fully compiled");
              } else if (!compiled) {
                BalloonUtility.showMessage("Something doesn't compile in " + computeLabel(erroneous),
                    "Sierra cannot scan if your code does not compile");
              } else {
                // Scan not run, because of modified editors
                BalloonUtility.showMessage("Modified editors", "Sierra did not run a scan due to unsaved editors");
              }
            } catch (CoreException e) {
              LOG.log(Level.SEVERE, I18N.err(86), e);
            }
            return Status.OK_STATUS;
          }
        };
        // one per workspace
        job.setRule(ResourcesPlugin.getWorkspace().getRoot());
        job.schedule();

        return Status.OK_STATUS;
      }
    }.schedule();
  }

  protected void showStartBalloon(final StringBuilder label) {
    if (EclipseUtility.getBooleanPreference(SierraPreferencesUtility.SHOW_BALLOON_NOTIFICATIONS)) {
      label.append(". ");
      label.append("You may continue your work. ");
      label.append("You will be notified when the");
      if (!isRescan) {
        label.append(" scan has completed.");
        BalloonUtility.showMessage("Sierra scan started", label.toString());
      } else {
        label.append(" re-scan has completed.");
        BalloonUtility.showMessage("Sierra re-scan started", label.toString());
      }
    }
  }

  protected final boolean setupConfigs(final List<Config> configs) {
    if (configs.isEmpty()) {
      return false;
    }
    if (!XUtil.useExperimental) {
      return true;
    }
    UIJob job = new UIJob("Put up test dialog") {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        final ScanTestCodeSelectionDialog dialog = new ScanTestCodeSelectionDialog(shell, configs);

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
    if (!XUtil.useExperimental) {
      return true;
    }
    List<Config> configs = new ArrayList<>();
    for (final ConfigCompilationUnit config : cuConfigs) {
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

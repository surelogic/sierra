package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.core.jobs.KeywordAccessRule;
import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.remote.AbstractRemoteSLJob;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.dialogs.ErrorDialogUtility;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.jdbc.project.ClientProjectManager;
import com.surelogic.sierra.tool.SierraToolConstants;

public final class DeleteProjectDataJob implements IRunnableWithProgress {

  final List<String> f_projectNames;

  final int f_jobFailureCode = 25;
  final String f_jobFailureMsg;

  public DeleteProjectDataJob(final List<String> projectNames) {
    f_projectNames = projectNames;
    f_jobFailureMsg = I18N.err(f_jobFailureCode, f_projectNames);
  }

  public void runJob() {
    try {
      PlatformUI
          .getWorkbench()
          .getProgressService()
          .runInUI(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), this,
              KeywordAccessRule.getInstance(JobConstants.ACCESS_KEY));
    } catch (Exception e) {
      SLLogger.getLogger().log(Level.SEVERE, f_jobFailureMsg, e);
    }
  }

  public static void utility(List<String> projectNames, Shell shell, boolean disconnect) {
    if (shell == null) {
      shell = EclipseUIUtility.getShell();
    }
    final boolean multiDelete = projectNames.size() > 1;

    final String title = "Confirm " + (multiDelete ? "Multiple Project" : "Project")
        + (disconnect ? " Disconnect" : " Sierra Data Deletion");
    final StringBuilder b = new StringBuilder();
    if (disconnect) {
      b.append("Disconnecting from the Sierra Team ");
      b.append("Server will automatically delete ");
      b.append("data in your Eclipse workspace about ");
      if (multiDelete) {
        b.append("these ").append(projectNames.size()).append(" projects,");
      } else {
        b.append("the project '").append(projectNames.get(0)).append("',");
      }
      b.append(" but will not change or delete any information that ");
      b.append("you have already published to the server.\n\n");
      b.append("Are you sure that you want to disconnect?");
    } else {
      b.append("Deleting the data in your Eclipse workspace about ");
      if (multiDelete) {
        b.append("these ").append(projectNames.size()).append(" projects,");
      } else {
        b.append("the project '").append(projectNames.get(0)).append("',");
      }
      b.append(" will not change or delete any information that ");
      b.append("you have already published to the server.\n\n");
      b.append("Are you should that you want to delete this ");
      b.append("data from your Eclipse workspace?");
    }
    if (MessageDialog.openConfirm(shell, title, b.toString())) {
      final DeleteProjectDataJob deleteProjectJob = new DeleteProjectDataJob(projectNames);
      deleteProjectJob.runJob();
    }
  }

  @Override
  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor, "Deleting selected projects");
    slMonitor.begin(f_projectNames.size() * 4);

    boolean jobFailed = false;
    try {
      /*
       * Delete the data in the database about these projects.
       */
      final Connection conn = Data.getInstance().transactionConnection();
      final ClientProjectManager manager = ClientProjectManager.getInstance(conn);
      try {
        for (final String projectName : f_projectNames) {
          try {
            manager.deleteProject(projectName, slMonitor);
            conn.commit();
          } catch (SQLException e) {
            conn.rollback();
            throw e;
          }
          ConnectedServerManager.getInstance().disconnect(projectName);
          DatabaseHub.getInstance().notifyProjectDeleted();
        }
      } catch (Exception e) {
        SLLogger.getLogger().log(Level.SEVERE, f_jobFailureMsg, e);
        jobFailed = true;
      } finally {
        conn.close();
      }
    } catch (SQLException e1) {
      SLLogger.getLogger().log(Level.SEVERE, f_jobFailureMsg, e1);
      jobFailed = true;
    }
    /*
     * Now we need to delete the scan document in the .sierra-data directory if
     * there is one.
     */
    for (final String projectName : f_projectNames) {
      if (projectName != null) {
        for (String suffix : SierraToolConstants.PARSED_FILE_SUFFIXES) {
          deleteProjectScanDocument(projectName, suffix);
          deleteProjectLogDocument(projectName);
        }
      }
    }
    monitor.done();
    DatabaseHub.getInstance().notifyProjectDeleted();
    if (jobFailed) {
      final UIJob job = new SLUIJob() {
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
          final IStatus reason = SLEclipseStatusUtility.createErrorStatus(f_jobFailureCode, f_jobFailureMsg);
          ErrorDialogUtility.open(null, null, reason);
          return Status.OK_STATUS;
        }
      };
      job.schedule();
    }
  }

  private void deleteProjectLogDocument(final String projectName) {
    for (File l : SierraPreferencesUtility.getSierraScanDirectory().listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(projectName + '.') && name.endsWith(AbstractRemoteSLJob.LOG_SUFFIX);
      }
    })) {
      l.delete();
    }
  }

  private void deleteProjectScanDocument(final String projectName, String suffix) {
    final File scanDocument = new File(SierraPreferencesUtility.getSierraScanDirectory(), projectName + suffix);
    if (scanDocument.exists() && scanDocument.isFile()) {
      final boolean success = scanDocument.delete();
      if (!success) {
        SLLogger.getLogger().warning(I18N.err(11, scanDocument.getAbsolutePath()));
      }
    }
  }
}

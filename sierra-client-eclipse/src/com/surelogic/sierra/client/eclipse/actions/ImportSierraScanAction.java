package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.jobs.ImportScanDocumentJob;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.tool.SierraToolConstants;

/**
 * This imports an Ant/Maven scan.
 * <p>
 * It uses the filename to determine the project which could create problems in
 * the future.
 */
public class ImportSierraScanAction implements IWorkbenchWindowActionDelegate {

  @Override
  public void run(IAction action) {
    final FileDialog fd = new FileDialog(EclipseUIUtility.getShell(), SWT.OPEN);
    fd.setText("Import Sierra Ant/Maven Scan");
    fd.setFilterExtensions(new String[] { "*" + SierraToolConstants.SIERRA_SCAN_TASK_SUFFIX, "*.*" });
    fd.setFilterNames(new String[] { "Compressed Sierra Scan Documents (*" + SierraToolConstants.SIERRA_SCAN_TASK_SUFFIX + ")",
        "All Files (*.*)" });
    final String name = fd.open();
    if (name != null) {
      SLUIJob showErrorDialog = new SLUIJob() {
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
          MessageDialog.openInformation(EclipseUIUtility.getShell(), I18N.msg("sierra.dialog.importscan.error.title"),
              I18N.msg("sierra.dialog.importscan.error.msg", name));
          return Status.OK_STATUS;
        }
      };

      final File zipFile = new File(name);
      // determine name of new scan directory from zip file name
      final String simpleName = zipFile.getName();
      if (!simpleName.toLowerCase().endsWith(".zip")) {
        showErrorDialog.schedule();
        return;
      }
      // get prefix to clear out old scan
      String namePrefix = zipFile.getName();
      int takeOff = SierraToolConstants.SIERRA_SCAN_TASK_SUFFIX.length() + 27 /* timestamp */;
      if (namePrefix.length() <= takeOff) {
        SLLogger.getLogger().log(Level.SEVERE, I18N.err(348, namePrefix), new Exception());
        showErrorDialog.schedule();
        return;
      }
      namePrefix = namePrefix.substring(0, namePrefix.length() - takeOff);
      final File targetDir = SierraPreferencesUtility.getSierraScanDirectory();
      // clear out old scan
      FileUtility.deleteFilesWithPrefix(targetDir, namePrefix);
      // unzip new one
      try {
        FileUtility.unzipFile(zipFile, targetDir);
      } catch (Exception e) {
        SLLogger.getLogger().log(Level.SEVERE, I18N.err(347, zipFile.getAbsolutePath(), targetDir.getAbsolutePath()), e);
        showErrorDialog.schedule();
        return;
      }

      final String projectName = namePrefix;
      File scan = new File(SierraPreferencesUtility.getSierraScanDirectory(), projectName + SierraToolConstants.PARSED_FILE_SUFFIX);
      if (scan.isFile()) {
        final Runnable runAfterImport = new Runnable() {
          @Override
          public void run() {
            /* Notify that scan was completed */
            DatabaseHub.getInstance().notifyScanLoaded();
            SLUIJob job = new SLUIJob() {

              @Override
              public IStatus runInUIThread(IProgressMonitor monitor) {
                MessageDialog.openInformation(EclipseUIUtility.getShell(), I18N.msg("sierra.dialog.importScan.success.title"),
                    I18N.msg("sierra.dialog.importScan.success.msg", projectName));
                return Status.OK_STATUS;
              }
            };
            job.schedule();
          }
        };
        ImportScanDocumentJob job = new ImportScanDocumentJob(scan, projectName, runAfterImport);
        job.schedule();
      } else {
        SLLogger.getLogger().log(Level.SEVERE, I18N.err(349, scan.getAbsolutePath()), new Exception());
        showErrorDialog.schedule();
      }
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    // Nothing to do
  }

  @Override
  public void dispose() {
    // Nothing to do
  }

  @Override
  public void init(IWorkbenchWindow window) {
    // Nothing to do
  }
}

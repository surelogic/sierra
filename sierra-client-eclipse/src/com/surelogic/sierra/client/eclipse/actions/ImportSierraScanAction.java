package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;

public class ImportSierraScanAction implements IWorkbenchWindowActionDelegate {

  @Override
  public void run(IAction action) {
    Shell shell = EclipseUIUtility.getShell();
    DirectoryDialog dd = new DirectoryDialog(shell);
    String fileName = dd.open();
    if (fileName != null) {
      File f = new File(fileName);
      File scanDirectory = SierraPreferencesUtility.getSierraScanDirectory();
      if (f.exists() && !f.isDirectory()) {
        FileUtility.copy(f, new File(scanDirectory, f.getName()));
        // TODO Do the sierra import to the db and update ui
      } else {
        MessageDialog.openError(shell, I18N.msg("flashlight.dialog.importScan.error.title"),
            I18N.msg("flashlight.dialog.importScan.error.title", f.getAbsolutePath()));
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

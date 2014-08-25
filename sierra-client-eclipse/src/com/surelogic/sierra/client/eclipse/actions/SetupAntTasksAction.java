package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.sierra.client.eclipse.LibResources;

public class SetupAntTasksAction implements IWorkbenchWindowActionDelegate {

  @Override
  public void run(IAction action) {
    DirectoryDialog dialog = new DirectoryDialog(EclipseUIUtility.getShell());
    dialog.setText(I18N.msg("sierra.eclipse.dialog.promises.saveAs.title"));
    dialog.setMessage(I18N.msg("sierra.eclipse.dialog.promises.saveAs.msg", LibResources.ANT_TASK_VERSION));
    final String result = dialog.open();
    if (result != null) {
      final File dir = new File(result, LibResources.ANT_TASK_VERSION);
      try {
        if (!dir.exists()) {
          dir.mkdirs();
        }
        final File tmp = File.createTempFile("ser", "zip");
        try {
          FileUtility.copy(LibResources.ANT_TASK_ZIP, LibResources.getAntTaskZip(), tmp);
          FileUtility.unzipFile(tmp, dir);
        } finally {
          tmp.delete();
        }
      } catch (IOException e) {
        SLLogger.getLogger().log(Level.SEVERE, I18N.err(225, LibResources.ANT_TASK_ZIP, dir.getAbsolutePath()), e);
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

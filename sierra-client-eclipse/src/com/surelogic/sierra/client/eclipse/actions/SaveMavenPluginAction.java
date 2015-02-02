package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.common.FileUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.dialogs.ErrorDialogUtility;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.LibResources;

public class SaveMavenPluginAction implements IWorkbenchWindowActionDelegate {

  private static final String MAVEN_ZIP_FORMAT = "sierra-maven-%s.zip";

  @Override
  public void run(IAction action) {
    String target = String.format(MAVEN_ZIP_FORMAT, Activator.getVersion());
    DirectoryDialog dialog = new DirectoryDialog(EclipseUIUtility.getShell());
    dialog.setText(I18N.msg("sierra.eclipse.dialog.maven.saveAs.title"));
    dialog.setMessage(I18N.msg("sierra.eclipse.dialog.maven.saveAs.msg", target));
    final String result = dialog.open();
    boolean copySuccessful = true;
    Exception ioException = null;
    if (result != null) {
      final File file = new File(result, target);
      try {
        if (file.exists()) {
          MessageDialog.openInformation(EclipseUIUtility.getShell(), I18N.msg("sierra.eclipse.dialog.maven.saveAs.failed.title"),
              I18N.msg("sierra.eclipse.dialog.maven.saveAs.exists.msg", file.getPath()));
          return;
        }
        copySuccessful = FileUtility.copy(LibResources.MAVEN_PLUGIN_ZIP, LibResources.getAntTaskZip(), file);
        MessageDialog.openInformation(EclipseUIUtility.getShell(), I18N.msg("sierra.eclipse.dialog.maven.saveAs.confirm.title"),
            I18N.msg("sierra.eclipse.dialog.maven.saveAs.confirm.msg", file.getPath()));
      } catch (IOException e) {
        ioException = e;
      }
      if (!copySuccessful) {
        final int err = 225;
        final String msg = I18N.err(225, LibResources.MAVEN_PLUGIN_ZIP, file.getAbsolutePath());
        final IStatus reason = SLEclipseStatusUtility.createErrorStatus(err, msg, ioException);
        ErrorDialogUtility.open(EclipseUIUtility.getShell(), I18N.msg("sierra.eclipse.dialog.maven.saveAs.failed.title"), reason,
            true);
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

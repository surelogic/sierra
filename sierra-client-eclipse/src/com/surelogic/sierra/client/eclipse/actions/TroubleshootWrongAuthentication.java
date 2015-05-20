package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.ServerLocation;

public final class TroubleshootWrongAuthentication extends TroubleshootConnection {

  public TroubleshootWrongAuthentication(final ServerFailureReport strategy, final ServerLocation location) {
    super(strategy, location);
  }

  @Override
  protected String getLabel() {
    return "Unable To Authenticate";
  }

  @Override
  protected IStatus createStatus() {
    return SLEclipseStatusUtility.createInfoStatus("Unable to authenticate to " + getLocation().createHomeURL().toString());
  }

  ServerLocation f_modifiedLocation = null;

  @Override
  protected ServerLocation showDialog() {
    final ServerLocation s = getLocation();
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        f_modifiedLocation = ServerAuthenticationDialog.open(null, getLocation());
      }
    });
    if (f_modifiedLocation == s) {
      setRetry(false);
    }
    return f_modifiedLocation;
  }
}

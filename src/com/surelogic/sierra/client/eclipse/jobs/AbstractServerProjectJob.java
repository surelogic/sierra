package com.surelogic.sierra.client.eclipse.jobs;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.*;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public abstract class AbstractServerProjectJob extends DatabaseJob {
  protected final SierraServer f_server;
  protected final String f_projectName;
  protected final ServerProjectGroupJob joinJob;
  
  
  public AbstractServerProjectJob(ServerProjectGroupJob family, String name, 
                                  SierraServer server, String project) {
    super(family, name);
    joinJob = family;
    joinJob.add(this);
    f_server = server;
    f_projectName = project;
  }
  
  public static final TroubleshootConnection 
  getTroubleshootConnection(SierraServer s, String proj, SierraServiceClientException e) {
	  if (e instanceof InvalidLoginException) {
		  return new TroubleshootWrongAuthentication(s, proj);
	  } else {
		  return new TroubleshootNoSuchServer(s, proj);
	  }
  }
  
  protected final TroubleshootConnection getTroubleshootConnection(SierraServiceClientException e) {
	  return getTroubleshootConnection(f_server, f_projectName, e);
  }
  
  protected final IStatus createWarningStatus(final int errNo, Throwable t) {
    final String msg = I18N.err(errNo, f_projectName, f_server);
    final IStatus s = SLStatus.createWarningStatus(errNo, msg, t);
    final String title = "Problem while "+getName();
    showErrorDialog(msg, t, title, s);
    return s;
  }
  
  protected final IStatus createErrorStatus(final int errNo, Throwable t) {
	  final String msg = I18N.err(errNo, f_projectName, f_server);
	  final IStatus s = SLStatus.createErrorStatus(errNo, msg, t);
	  final String title = "Error while "+getName();
	  showErrorDialog(msg, t, title, s);
	  return s;
  }

  public void showErrorDialog(final String msg, Throwable t, 
		                       final String title, final IStatus s) {
	  SLLogger.log(Level.INFO, msg, t);
	  Job job = new SLUIJob() {
		  @Override
		  public IStatus runInUIThread(IProgressMonitor monitor) {
			  // Note this will be automatically logged by Eclipse
			  // when the original job returns 
			  ErrorDialogUtility.open(null, title, s, false);
			  return Status.OK_STATUS;
		  } 

	  };
	  job.schedule();
  }
}

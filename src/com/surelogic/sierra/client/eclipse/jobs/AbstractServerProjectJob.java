package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IStatus;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
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
  
  protected final TroubleshootConnection getTroubleshootConnection(SierraServiceClientException e) {
    if (e instanceof InvalidLoginException) {
      return new TroubleshootWrongAuthentication(f_server,
          f_projectName);
    } else {
      return new TroubleshootNoSuchServer(f_server,
          f_projectName);
    }
  }
  
  protected final IStatus createWarningStatus(final int errNo, Throwable t) {
    final String msg = I18N.err(errNo, f_projectName, f_server);
    return SLStatus.createWarningStatus(errNo, msg, t);
  }
}

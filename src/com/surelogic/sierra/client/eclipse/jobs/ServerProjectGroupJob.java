/*
 * Created on Feb 21, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.surelogic.sierra.client.eclipse.jobs;

import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.sierra.jdbc.settings.ConnectedServer;

/**
 * A Job designed to coordinate AbstractServerProjectJobs
 * 
 * @author Edwin
 */
public class ServerProjectGroupJob extends Job {
  public static final ConnectedServer[] NO_SERVERS = new ConnectedServer[0];
  
  private final List<AbstractServerProjectJob> jobs = new ArrayList<AbstractServerProjectJob>();
  private final Set<ConnectedServer> okServers, serversToProcess;
  
  public ServerProjectGroupJob(Set<ConnectedServer> servers) {
    super("Waiting for synchronize jobs");
    setSystem(true);
    okServers = servers;
    serversToProcess = new HashSet<ConnectedServer>(servers);
  }

  void add(AbstractServerProjectJob j) {
    jobs.add(j);
  }
  
  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      Job.getJobManager().join(this, monitor);
    } catch (OperationCanceledException e) {
      e.printStackTrace();
      return Status.CANCEL_STATUS;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return Status.CANCEL_STATUS;
    }       
    /*
    for(AbstractServerProjectJob j : jobs) {
      System.out.println(j.getResult());
    }
    */
    return Status.OK_STATUS;
  }

  public boolean process(ConnectedServer server) {
	  return serversToProcess.contains(server);	  
  }
  
  public void doneProcessing(ConnectedServer server) {
	  serversToProcess.remove(server);
  }
  
  /**
   * @return true if we should bother trying to troubleshoot
   */
  public boolean troubleshoot(ConnectedServer server) {
      return okServers.contains(server);
  }
  
  /**
   * Mark the server as failed
   */
  public void fail(ConnectedServer server) {
	  okServers.remove(server);
	  doneProcessing(server);
  }
}

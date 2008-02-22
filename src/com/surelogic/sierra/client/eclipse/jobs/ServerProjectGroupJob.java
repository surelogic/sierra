/*
 * Created on Feb 21, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.surelogic.sierra.client.eclipse.jobs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.sierra.client.eclipse.model.SierraServer;

/**
 * A Job designed to coordinate AbstractServerProjectJobs
 * 
 * @author Edwin
 */
public class ServerProjectGroupJob extends Job {
  public static final SierraServer[] NO_SERVERS = new SierraServer[0];
  
  private List<AbstractServerProjectJob> jobs = new ArrayList<AbstractServerProjectJob>();
  private SierraServer[] servers;
  
  public ServerProjectGroupJob(SierraServer... servers) {
    super("Waiting for synchronize jobs");
    setSystem(true);
    this.servers = servers;
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
    for(AbstractServerProjectJob j : jobs) {
      System.out.println(j.getResult());
    }
    return Status.OK_STATUS;
  }

  /**
   * @return true if we should bother trying to troubleshoot
   */
  public boolean troubleshoot(SierraServer server) {
    for(SierraServer s : servers) {
      if (server == s) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Mark the server as failed
   */
  public void fail(SierraServer server) {
    for(int i=0; i<servers.length; i++) {
      if (servers[i] == server) {
        servers[i] = null;
      }
    }
  }
}

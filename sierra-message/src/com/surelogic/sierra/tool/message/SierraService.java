package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.Service;

@Service(version = "2.2.1")
public interface SierraService {

  /**
   * Publish a run to the server.
   * 
   * @param run
   * @return whether or not the run was successfully generated on the server.
   */
  void publishRun(Scan scan) throws ScanVersionException;

  /**
   * Get the set of available timeseries names.
   * 
   * @return
   */
  Timeseries getTimeseries(TimeseriesRequest request);

  SyncResponse synchronize(SyncRequest request) throws ServerMismatchException;

  /**
   * Synchronize the projects between a client and server.
   * 
   * @param request
   * @return
   * @throws ServerMismatchException
   */
  SyncProjectResponse synchronizeProject(SyncProjectRequest request) throws ServerMismatchException;

}

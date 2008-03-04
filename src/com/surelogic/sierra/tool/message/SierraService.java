package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.Service;

public interface SierraService extends Service {
	/**
	 * Publish a run to the server.
	 * 
	 * @param run
	 * @return whether or not the run was successfully generated on the server.
	 */
	void publishRun(Scan scan);

	/**
	 * Get the set of available timeseries names.
	 * 
	 * @return
	 */
	Timeseries getTimeseries(TimeseriesRequest request);

	/**
	 * Return the server's unique identifier
	 * 
	 * @return
	 */
	ServerUIDReply getUid(ServerUIDRequest request);

	/**
	 * Synchronize the projects between a client and server.
	 * 
	 * @param request
	 * @return
	 * @throws ServerMismatchException
	 */
	SyncResponse synchronizeProject(SyncRequest request)
			throws ServerMismatchException;

	/**
	 * Return the settings associated with a given timeseries.
	 * 
	 * @param request
	 * @return the settings associated with a given timeseries.
	 */
	SettingsReply getSettings(SettingsRequest request)
			throws ServerMismatchException;

	/**
	 * 
	 * @param request
	 * @return the global settings associated with this server.
	 */
	GlobalSettings getGlobalSettings(GlobalSettingsRequest request);

	void writeGlobalSettings(GlobalSettings settings);
}

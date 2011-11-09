package com.surelogic.sierra.client.eclipse.model;

import com.surelogic.sierra.jdbc.settings.ConnectedServer;

/**
 * This class track connection statistics between the client and a server within
 * a single Eclipse session. It is not persisted. The server it is associated
 * with is tracked by its associated {@link ConnectedServerManager}.
 * 
 * @see ConnectedServerManager#getStats(ConnectedServer)
 * 
 * @author Edwin.Chan
 */
public class ConnectedServerStats {
	public final String f_serverId;
	private int f_problemCount;
	private boolean usedDuringThisSession = false;

	ConnectedServerStats(ConnectedServer server) {
		f_serverId = server.getUuid();
	}

	public void markAsConnected() {
		f_problemCount = 0;
	}

	public int getProblemCount() {
		return f_problemCount;
	}

	public void encounteredProblem() {
		f_problemCount++;
	}

	public boolean usedToConnectToAServer() {
		return usedDuringThisSession;
	}

	public void setUsed() {
		usedDuringThisSession = true;
	}
}

package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ManageServerService extends RemoteService {

	ServerInfo getServerInfo();
	
	ServerInfo deploySchema();
	
	ServerInfo setEmail(String address);
	
}

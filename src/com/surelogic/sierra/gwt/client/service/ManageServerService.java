package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.ServerInfo;

public interface ManageServerService extends RemoteService {

	ServerInfo getServerInfo();
	
	ServerInfo deploySchema();
	
	ServerInfo setEmail(String address);
	
}

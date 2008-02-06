package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;

public interface SessionService extends RemoteService {

	boolean isValidSession();
	
	String login(String username, String password);
	
}

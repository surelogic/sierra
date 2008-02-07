package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.LoginResult;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public interface SessionService extends RemoteService {

	UserAccount getUserAccount();

	LoginResult login(String username, String password);

	void logout();
	
}

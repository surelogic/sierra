package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.LoginResult;

public interface SessionService extends RemoteService {

	UserAccount getUserAccount();

	LoginResult login(String username, String password);

}

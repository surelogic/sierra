package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SessionServiceAsync {

	void getUserAccount(AsyncCallback callback);

	void login(String username, String password, AsyncCallback callback);

	void logout(AsyncCallback callback);
	
}

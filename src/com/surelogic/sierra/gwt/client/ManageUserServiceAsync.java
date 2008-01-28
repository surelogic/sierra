package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ManageUserServiceAsync {

	void getUsers(AsyncCallback callback);

	void findUser(String userQueryString, AsyncCallback callback);
	
	void createUser(String userName, String password, AsyncCallback callback);
	
}

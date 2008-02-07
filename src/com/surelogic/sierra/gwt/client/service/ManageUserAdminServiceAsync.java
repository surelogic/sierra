package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ManageUserAdminServiceAsync {

	void isAvailable(AsyncCallback callback);
	
	void getUsers(AsyncCallback callback);

	void findUser(String userQueryString, AsyncCallback callback);
	
	void createUser(String userName, String password, AsyncCallback callback);
	
	void updateUser(String user, String password, boolean isAdmin, AsyncCallback callback);
	
	void getUserInfo(String user, AsyncCallback callback);
	
	void deleteUser(String user, AsyncCallback callback);
}

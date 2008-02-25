package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public interface ManageUserAdminServiceAsync {

	void isAvailable(AsyncCallback callback);

	void getUsers(AsyncCallback callback);

	void findUser(String userQueryString, AsyncCallback callback);

	void createUser(UserAccount account, String password, AsyncCallback callback);

	void updateUser(UserAccount account, String password, AsyncCallback callback);

	void getUserInfo(String user, AsyncCallback callback);

	void updateUsersStatus(List users, boolean status, AsyncCallback callback);
}

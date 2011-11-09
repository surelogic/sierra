package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public interface ManageUserAdminServiceAsync {

	void isAvailable(AsyncCallback<Boolean> callback);

	void getUsers(AsyncCallback<List<UserAccount>> callback);

	void findUser(String userQueryString,
			AsyncCallback<List<UserAccount>> callback);

	void createUser(UserAccount account, String password,
			AsyncCallback<Result<String>> callback);

	void changeUserPassword(String targetUser, String currentUserPassword,
			String newPassword, AsyncCallback<Result<String>> callback);

	void updateUser(UserAccount account,
			AsyncCallback<Result<UserAccount>> callback);

	void getUserInfo(String user, AsyncCallback<UserAccount> callback);

	void updateUsersStatus(List<String> users, boolean status,
			AsyncCallback<Void> callback);
}

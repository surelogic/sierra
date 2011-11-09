package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ServerType;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public interface SessionServiceAsync {

	void getUserAccount(AsyncCallback<Result<UserAccount>> callback);

	void login(String username, String password,
			AsyncCallback<Result<UserAccount>> callback);

	void logout(AsyncCallback<Result<String>> callback);

	void getServerType(AsyncCallback<ServerType> callback);

}

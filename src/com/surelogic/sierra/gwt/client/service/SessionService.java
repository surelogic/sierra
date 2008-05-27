package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public interface SessionService extends RemoteService {

	Result<UserAccount> getUserAccount();

	Result<UserAccount> login(String username, String password);

	Result<String> logout();

}

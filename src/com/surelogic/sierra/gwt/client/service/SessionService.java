package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.Result;

public interface SessionService extends RemoteService {

	Result getUserAccount();

	Result login(String username, String password);

	Result logout();

}

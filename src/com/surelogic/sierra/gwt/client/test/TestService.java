package com.surelogic.sierra.gwt.client.test;

import com.google.gwt.user.client.rpc.RemoteService;

public interface TestService extends RemoteService {

	public String toUpperCase(String text);

}

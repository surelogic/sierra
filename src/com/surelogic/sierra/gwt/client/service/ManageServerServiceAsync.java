package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ManageServerServiceAsync {

	void getServerInfo(AsyncCallback callback);

	void deploySchema(AsyncCallback callback);

	void setEmail(String address, AsyncCallback callback);

}

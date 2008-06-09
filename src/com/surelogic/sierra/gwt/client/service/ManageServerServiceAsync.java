package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.EmailInfo;
import com.surelogic.sierra.gwt.client.data.ServerInfo;

public interface ManageServerServiceAsync {

	void getServerInfo(AsyncCallback<ServerInfo> callback);

	void deploySchema(AsyncCallback<ServerInfo> callback);

	void setEmail(EmailInfo email, AsyncCallback<ServerInfo> callback);

	void testAdminEmail(AsyncCallback<Void> callback);
}

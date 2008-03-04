package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.EmailInfo;

public interface ManageServerServiceAsync {

	void getServerInfo(AsyncCallback callback);

	void deploySchema(AsyncCallback callback);

	void setEmail(EmailInfo email, AsyncCallback callback);

	void testAdminEmail(AsyncCallback callback);
}

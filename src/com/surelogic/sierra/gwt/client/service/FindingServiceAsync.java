package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FindingServiceAsync {
	void getFinding(String id, AsyncCallback callback);
}

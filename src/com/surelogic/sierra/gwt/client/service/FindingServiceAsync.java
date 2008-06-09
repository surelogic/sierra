package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.data.Result;

public interface FindingServiceAsync {
	void getFinding(String id, AsyncCallback<Result<FindingOverview>> callback);
}

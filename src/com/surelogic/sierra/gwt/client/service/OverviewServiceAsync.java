package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OverviewServiceAsync {
	void getProjectOverviews(AsyncCallback callback);

	void getUserOverviews(AsyncCallback callback);
}

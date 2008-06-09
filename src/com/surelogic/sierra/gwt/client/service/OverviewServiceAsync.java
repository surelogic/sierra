package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.UserOverview;

public interface OverviewServiceAsync {
	void getProjectOverviews(AsyncCallback<List<ProjectOverview>> callback);

	void getUserOverviews(AsyncCallback<List<UserOverview>> callback);
}

package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;

public interface OverviewServiceAsync {
	void getProjectOverviews(AsyncCallback<List<ProjectOverview>> callback);

}

package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.UserOverview;

public interface OverviewService extends RemoteService {

	/**
	 * @return a list of the projects currently on the server.
	 */
	List<ProjectOverview> getProjectOverviews();

	/**
	 * @return a list of the users currently active on the server.
	 */
	List<UserOverview> getUserOverviews();
}

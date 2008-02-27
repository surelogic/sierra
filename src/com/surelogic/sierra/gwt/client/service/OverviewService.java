package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

public interface OverviewService extends RemoteService {

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.ProjectOverview>
	 * @return a list of the projects currently on the server.
	 */
	List getProjectOverviews();

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.UserOverview>
	 * @return a list of the users currently active on the server.
	 */
	List getUserOverviews();
}

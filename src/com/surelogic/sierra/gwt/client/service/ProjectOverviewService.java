package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ProjectOverviewService extends RemoteService {

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.ProjectOverview>
	 * @return a list of the projects currently on the server.
	 */
	List getProjectOverviews();

}

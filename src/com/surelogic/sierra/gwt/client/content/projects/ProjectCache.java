package com.surelogic.sierra.gwt.client.content.projects;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ProjectCache extends Cache<Project> {

	@Override
	protected void doRefreshCall(AsyncCallback<List<Project>> callback) {
		ServiceHelper.getSettingsService().getProjects(callback);
	}

	@Override
	protected void doSaveCall(Project item, AsyncCallback<Status> callback) {
		callback.onSuccess(Status.failure("Not implemented"));
	}
}

package com.surelogic.sierra.gwt.client.content.servermgmt;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.Server;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ServerCache extends Cache<Server> {

	@Override
	protected void doRefreshCall(AsyncCallback<List<Server>> callback) {
		ServiceHelper.getSettingsService().listServerLocations(callback);
	}

	@Override
	protected void doSaveCall(Server item, AsyncCallback<Status> callback) {
		callback.onSuccess(Status.failure("Not implemented"));
	}

}

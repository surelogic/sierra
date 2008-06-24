package com.surelogic.sierra.gwt.client.data.cache;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.ServerLocation;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ServerCache extends Cache<ServerLocation> {

	@Override
	protected void doRefreshCall(AsyncCallback<List<ServerLocation>> callback) {
		ServiceHelper.getSettingsService().listServerLocations(callback);
	}

	@Override
	protected void doSaveCall(ServerLocation item, AsyncCallback<Status> callback) {
		callback.onSuccess(Status.failure("Not implemented"));
	}

}

package com.surelogic.sierra.gwt.client.data.cache;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.ServerLocation;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ServerLocationCache extends Cache<ServerLocation> {

	@Override
	protected void doRefreshCall(
			final AsyncCallback<List<ServerLocation>> callback) {
		ServiceHelper.getSettingsService().listServerLocations(callback);
	}

	@Override
	protected void doSaveCall(final ServerLocation item,
			final AsyncCallback<Status> callback) {
		ServiceHelper.getSettingsService().saveServerLocation(item, callback);
	}

}

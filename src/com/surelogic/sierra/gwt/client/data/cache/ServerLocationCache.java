package com.surelogic.sierra.gwt.client.data.cache;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.PortalServerLocation;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ServerLocationCache extends Cache<PortalServerLocation> {

	@Override
	protected void doRefreshCall(
			final AsyncCallback<List<PortalServerLocation>> callback) {
		ServiceHelper.getSettingsService().listServerLocations(callback);
	}

	@Override
	protected void doSaveCall(final PortalServerLocation item,
			final AsyncCallback<Status> callback) {
		ServiceHelper.getSettingsService().saveServerLocation(item, callback);
	}

}

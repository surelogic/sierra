package com.surelogic.sierra.gwt.client.data.cache;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Extension;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ExtensionCache extends Cache<Extension> {
	private static final ExtensionCache INSTANCE = new ExtensionCache();

	@Override
	protected void doRefreshCall(final AsyncCallback<List<Extension>> callback) {
		ServiceHelper.getSettingsService().listExtensions(callback);
	}

	@Override
	protected void doSaveCall(final Extension item,
			final AsyncCallback<Status> callback) {
		throw new UnsupportedOperationException(
				"Extensions may not be updated in the portal.");
	}

	public static ExtensionCache getInstance() {
		return INSTANCE;
	}

}

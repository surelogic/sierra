package com.surelogic.sierra.gwt.client.data.cache;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.*;

public final class FindingTypeCache extends Cache<FindingType> {

	private static final FindingTypeCache INSTANCE = new FindingTypeCache();
	private final SettingsServiceAsync settings;
	
	private FindingTypeCache() {
		super();
		settings = ServiceHelper.getSettingsService();
	}

	@Override
	protected void doRefreshCall(final AsyncCallback<List<FindingType>> callback) {
		settings.getFindingTypes(callback);
	}

	@Override
	protected void doSaveCall(final FindingType item,
			final AsyncCallback<Status> callback) {
		callback.onSuccess(Status.failure("Not implemented"));
	}

	public static FindingTypeCache getInstance() {
		return INSTANCE;
	}

}

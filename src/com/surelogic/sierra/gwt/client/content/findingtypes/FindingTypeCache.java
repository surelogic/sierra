package com.surelogic.sierra.gwt.client.content.findingtypes;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class FindingTypeCache extends Cache<FindingType> {

	@Override
	protected void doRefreshCall(AsyncCallback<List<FindingType>> callback) {
		ServiceHelper.getSettingsService().getFindingTypes(callback);
	}

	@Override
	protected void doSaveCall(FindingType item, AsyncCallback<Status> callback) {
		callback.onSuccess(Status.failure("Not implemented"));
	}

}

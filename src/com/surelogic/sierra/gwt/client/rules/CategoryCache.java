package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class CategoryCache extends Cache {

	protected void doRefreshCall(AsyncCallback callback) {
		ServiceHelper.getSettingsService().getCategories(callback);
	}

	protected void doSaveCall(AsyncCallback callback) {
		// TODO Auto-generated method stub

	}
}

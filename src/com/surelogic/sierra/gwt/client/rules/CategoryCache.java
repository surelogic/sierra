package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class CategoryCache extends Cache<Category> {

	protected void doRefreshCall(AsyncCallback callback) {
		ServiceHelper.getSettingsService().getCategories(callback);
	}

	protected void doSaveCall(Cacheable item, AsyncCallback callback) {
		ServiceHelper.getSettingsService().updateCategory((Category) item,
				callback);
	}
}

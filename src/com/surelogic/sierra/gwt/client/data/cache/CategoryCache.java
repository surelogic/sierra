package com.surelogic.sierra.gwt.client.data.cache;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class CategoryCache extends Cache<Category> {
	private static final CategoryCache instance = new CategoryCache();

	public static CategoryCache getInstance() {
		return instance;
	}

	private CategoryCache() {
		super();
		// singleton
	}

	@Override
	protected void doRefreshCall(AsyncCallback<List<Category>> callback) {
		ServiceHelper.getSettingsService().getCategories(callback);
	}

	@Override
	protected void doSaveCall(Category item, AsyncCallback<Status> callback) {
		ServiceHelper.getSettingsService().updateCategory(item, callback);
	}
}

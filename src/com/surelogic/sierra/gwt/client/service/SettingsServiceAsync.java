package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Category;

public interface SettingsServiceAsync {

	void searchFindingTypes(String query, int limit, AsyncCallback callback);

	void searchCategories(String query, int limit, AsyncCallback callback);

	void getCategories(AsyncCallback callback);

	void createCategory(String name, List entries, List parents,
			AsyncCallback callback);

	void updateCategory(Category c, AsyncCallback callback);

	void getScanFilters(AsyncCallback callback);

	void getFindingTypeInfo(String uid, AsyncCallback callback);

	void createScanFilter(String name, AsyncCallback callback);

}

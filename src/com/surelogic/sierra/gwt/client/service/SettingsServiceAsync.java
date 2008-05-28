package com.surelogic.sierra.gwt.client.service;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeInfo;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;

public interface SettingsServiceAsync {

	void searchFindingTypes(String query, int limit,
			AsyncCallback<Map<String, String>> callback);

	void searchCategories(String query, int limit,
			AsyncCallback<Map<String, String>> callback);

	void getCategories(AsyncCallback<List<Category>> callback);

	void createCategory(String name, List<String> entries,
			List<String> parents, AsyncCallback<Result<String>> callback);

	void updateCategory(Category c, AsyncCallback<Status> callback);

	void deleteCategory(String uuid, AsyncCallback<Status> callback);

	void getScanFilters(AsyncCallback<List<ScanFilter>> callback);

	void getFindingTypeInfo(String uid,
			AsyncCallback<Result<FindingTypeInfo>> callback);

	void createScanFilter(String name, AsyncCallback<ScanFilter> callback);

	void updateScanFilter(ScanFilter f, AsyncCallback<Status> callback);

	void deleteScanFilter(String uuid, AsyncCallback<Status> callback);
}

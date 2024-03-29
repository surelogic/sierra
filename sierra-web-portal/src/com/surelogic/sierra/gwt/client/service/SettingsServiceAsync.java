package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Extension;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.PortalServerLocation;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings;

public interface SettingsServiceAsync {

	void searchProjects(String query, int limit,
			AsyncCallback<List<String>> asyncCallback);

	void getProjects(AsyncCallback<List<Project>> callback);

	void saveProjectFilter(String project, String scanFilter,
			AsyncCallback<Status> callback);

	void getCategories(AsyncCallback<List<Category>> callback);

	void createCategory(String name, List<String> entries,
			List<String> parents, AsyncCallback<Result<String>> callback);

	void updateCategory(Category c, AsyncCallback<Status> callback);

	void duplicateCategory(String newName, Category source,
			AsyncCallback<Result<String>> callback);

	void deleteCategory(String uuid, AsyncCallback<Status> callback);

	void getScanFilters(AsyncCallback<List<ScanFilter>> callback);

	void getFindingTypes(AsyncCallback<List<FindingType>> callback);

	void getFindingType(String uid, AsyncCallback<Result<FindingType>> callback);

	void createScanFilter(String name, AsyncCallback<ScanFilter> callback);

	void updateScanFilter(ScanFilter f, AsyncCallback<Status> callback);

	void deleteScanFilter(String uuid, AsyncCallback<Status> callback);

	void setDefaultScanFilter(ScanFilter f, AsyncCallback<Status> callback);

	void listServerLocations(AsyncCallback<List<PortalServerLocation>> callback);

	void deleteServerLocation(String uuid, AsyncCallback<Status> callback);

	void saveServerLocation(PortalServerLocation loc,
			AsyncCallback<Status> callback);

	void listReportSettings(AsyncCallback<List<ReportSettings>> callback);

	void saveReportSettings(ReportSettings settings,
			AsyncCallback<Status> callback);

	void deleteReportSettings(String settings, AsyncCallback<Status> callback);

	void getDashboardSettings(AsyncCallback<DashboardSettings> callback);

	void saveDashboardSettings(DashboardSettings settings,
			AsyncCallback<Status> callback);

	void listExtensions(AsyncCallback<List<Extension>> callback);

}

package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.data.ScanDetail;

public interface FindingServiceAsync {
	void getFinding(String id, AsyncCallback<Result<FindingOverview>> callback);

	void getScans(String project, AsyncCallback<List<Scan>> callback);

	void getScanDetail(String uuid, AsyncCallback<ScanDetail> callback);
}

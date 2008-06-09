package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ScanFilterCache extends Cache<ScanFilter> {

	@Override
	protected void doRefreshCall(AsyncCallback<List<ScanFilter>> callback) {
		ServiceHelper.getSettingsService().getScanFilters(callback);

	}

	@Override
	protected void doSaveCall(ScanFilter item, AsyncCallback<Status> callback) {
		ServiceHelper.getSettingsService().updateScanFilter(item, callback);
	}

}

package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ScanFilterCache extends Cache {

	protected void doRefreshCall(AsyncCallback callback) {
		ServiceHelper.getSettingsService().getScanFilters(callback);
	}

	protected void doSaveCall(Cacheable item, AsyncCallback callback) {
		ServiceHelper.getSettingsService().updateScanFilter((ScanFilter) item,
				callback);

	}

}

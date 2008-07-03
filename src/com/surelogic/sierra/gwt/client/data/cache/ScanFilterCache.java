package com.surelogic.sierra.gwt.client.data.cache;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ScanFilterCache extends Cache<ScanFilter> {

	private static final ScanFilterCache instance = new ScanFilterCache();

	public static ScanFilterCache getInstance() {
		return instance;
	}

	private ScanFilterCache() {
		super();
		// singleton
	}

	public ScanFilter getGlobalFilter() {
		for (final ScanFilter filter : this) {
			if ("GLOBAL".equals(filter.getName())) {
				return filter;
			}
		}
		return null;
	}

	@Override
	protected void doRefreshCall(AsyncCallback<List<ScanFilter>> callback) {
		ServiceHelper.getSettingsService().getScanFilters(callback);

	}

	@Override
	protected void doSaveCall(ScanFilter item, AsyncCallback<Status> callback) {
		ServiceHelper.getSettingsService().updateScanFilter(item, callback);
	}

}

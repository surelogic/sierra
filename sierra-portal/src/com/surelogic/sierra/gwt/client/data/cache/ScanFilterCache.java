package com.surelogic.sierra.gwt.client.data.cache;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public final class ScanFilterCache extends Cache<ScanFilter> {

	private static final ScanFilterCache instance = new ScanFilterCache();

	public static ScanFilterCache getInstance() {
		return instance;
	}

	private ScanFilterCache() {
		super();
		// singleton
	}

	@Override
	protected void doRefreshCall(final AsyncCallback<List<ScanFilter>> callback) {
		ServiceHelper.getSettingsService().getScanFilters(callback);

	}

	@Override
	protected void doSaveCall(final ScanFilter item,
			final AsyncCallback<Status> callback) {
		ServiceHelper.getSettingsService().updateScanFilter(item, callback);
	}

}

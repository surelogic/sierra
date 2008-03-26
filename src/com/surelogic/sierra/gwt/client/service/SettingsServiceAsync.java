package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SettingsServiceAsync {

	void getFilterSets(AsyncCallback callback);

	void createFilterSet(String name, List entries, List parents,
			AsyncCallback callback);
}

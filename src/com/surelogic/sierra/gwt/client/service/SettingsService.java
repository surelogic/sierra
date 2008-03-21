package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

public interface SettingsService extends RemoteService {
	/**
	 * 
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.FilterSet>
	 */
	List getFilterSets();

}

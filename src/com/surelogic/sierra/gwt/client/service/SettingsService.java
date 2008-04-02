package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.Status;

public interface SettingsService extends RemoteService {

	/**
	 * 
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.Category>
	 */
	List getCategories();

	/**
	 * 
	 * 
	 * @gwt.typeArgs entries <java.lang.String>
	 * @gwt.typeArgs parents <java.lang.String>
	 * 
	 * @param name
	 *            the new filter set's name
	 * @param entries
	 *            a list of finding type uuids to disallow
	 * @param parents
	 *            a list of parent uuids
	 * @return
	 */
	Status createFilterSet(String name, List entries, List parents);

}

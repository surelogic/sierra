package com.surelogic.sierra.gwt.client.service;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;

public interface SettingsService extends RemoteService {
	/**
	 * Search for a list of finding types that match the query string
	 * 
	 * @gwt.typeArgs <java.lang.String, java.lang.String>
	 * 
	 * @param query
	 * @return a map of key/name pairs
	 */
	Map searchFindingTypes(String query, int limit);

	/**
	 * Search for a list of categories that match the query string
	 * 
	 * @gwt.typeArgs <java.lang.String, java.lang.String>
	 * 
	 * @param query
	 * @return a map of key/name pairs
	 */
	Map searchCategories(String query, int limit);

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
	Status createCategory(String name, List entries, List parents);

	/**
	 * 
	 * @param c
	 *            the category to update
	 * @return the updated category, or a failure status
	 */
	Status updateCategory(Category c);

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.ScanFilter>
	 * @return
	 */
	List getScanFilters();

	/**
	 * Create a new scan filter with the given name
	 * 
	 * @param name
	 * @return
	 */
	ScanFilter createScanFilter(String name);

	/**
	 * 
	 * @param uid
	 * @return
	 */
	Result getFindingTypeInfo(String uid);

	Status updateScanFilter(ScanFilter f);
}

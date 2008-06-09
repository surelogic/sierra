package com.surelogic.sierra.gwt.client.service;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeInfo;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;

public interface SettingsService extends RemoteService {
	/**
	 * Search for a list of finding types that match the query string
	 * 
	 * 
	 * 
	 * @param query
	 * @return a map of key/name pairs
	 */
	Map<String, String> searchFindingTypes(String query, int limit);

	/**
	 * Search for a list of categories that match the query string
	 * 
	 * 
	 * 
	 * @param query
	 * @return a map of key/name pairs
	 */
	Map<String, String> searchCategories(String query, int limit);

	/**
	 * Search for a list of projects that match the query string
	 * 
	 * @param query
	 * @param limit
	 * @return a list of project names
	 */
	List<String> searchProjects(String query, int limit);

	/**
	 * 
	 * 
	 */
	List<Category> getCategories();

	/**
	 * 
	 * 
	 * 
	 * @param name
	 *            the new filter set's name
	 * @param entries
	 *            a list of finding type uuids to disallow
	 * @param parents
	 *            a list of parent uuids
	 * @return creation status along with the uuid of the new category
	 */
	Result<String> createCategory(String name, List<String> entries,
			List<String> parents);

	/**
	 * 
	 * @param c
	 *            the category to update
	 * @return the updated category, or a failure status
	 */
	Status updateCategory(Category c);

	/**
	 * Delete the category w/ the given uuid
	 * 
	 * @param uuid
	 * @return whether or not the deletion was successful
	 */
	Status deleteCategory(String uuid);

	/**
	 * @return
	 */
	List<ScanFilter> getScanFilters();

	/**
	 * Create a new scan filter with the given name
	 * 
	 * @param name
	 * @return
	 */
	ScanFilter createScanFilter(String name);

	/**
	 * Delete the scan filter with the given uuid
	 * 
	 * @param uuid
	 * @return whether or not the scan filter was deleted
	 */
	Status deleteScanFilter(String uuid);

	/**
	 * 
	 * @param uid
	 * @return
	 */
	Result<FindingTypeInfo> getFindingTypeInfo(String uid);

	Status updateScanFilter(ScanFilter f);
}

package com.surelogic.sierra.gwt.client.service;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.DashboardSettings;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ServerLocation;
import com.surelogic.sierra.gwt.client.data.Status;

public interface SettingsService extends RemoteService {
	/**
	 * Search for a list of finding types that match the query string
	 * 
	 * 
	 * 
	 * @param query
	 * @param limit
	 *            the number of results to return, or -1 for an unbounded number
	 * @return a map of key/name pairs
	 */
	Map<String, String> searchFindingTypes(String query, int limit);

	/**
	 * Search for a list of categories that match the query string
	 * 
	 * 
	 * 
	 * @param query
	 * @param limit
	 *            the number of results to return, or -1 for an unbounded number
	 * @return a map of key/name pairs
	 */
	Map<String, String> searchCategories(String query, int limit);

	/**
	 * Search for a list of projects that match the query string
	 * 
	 * @param query
	 * @param limit
	 *            the number of results to return, or -1 for an unbounded number
	 * @return a list of project names
	 */
	List<String> searchProjects(String query, int limit);

	/**
	 * 
	 */
	List<Project> getProjects();

	/**
	 * Save the given project and its associated scan filter.
	 * 
	 * @param project
	 *            the project's name
	 * @param scanFilter
	 *            a scan filter uuid
	 * @return
	 */
	Status saveProjectFilter(String project, String scanFilter);

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
	 * @return
	 */
	List<FindingType> getFindingTypes();

	/**
	 * 
	 * @param uuid
	 * @return
	 */
	Result<FindingType> getFindingType(String uuid);

	Status updateScanFilter(ScanFilter f);

	/**
	 * Return a list of the servers that this server currently connects to.
	 * 
	 * @return
	 */
	List<ServerLocation> listServerLocations();

	/**
	 * Delete the server location corresponding to this label.
	 * 
	 * @return
	 */
	Status deleteServerLocation(String label);

	/**
	 * Update a new server location with the given information. The label must
	 * be non-null and unique.
	 * 
	 * @param loc
	 * @return
	 */
	Status saveServerLocation(ServerLocation loc);

	/**
	 * Return a list of the report settings available to the user.
	 * 
	 * @return
	 */
	List<ReportSettings> listReportSettings();

	/**
	 * Return the user's dashboard preferences.
	 * 
	 * @return
	 */
	DashboardSettings getDashboardSettings();

	/**
	 * Save the given report settings.
	 * 
	 * @param settings
	 * @return
	 */
	Status saveReportSettings(ReportSettings settings);

	/**
	 * Delete the given report settings.
	 * 
	 * @param settings
	 * @return
	 */
	Status deleteReportSettings(String settings);

}

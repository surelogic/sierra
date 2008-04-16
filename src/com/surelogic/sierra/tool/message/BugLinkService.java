package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.Service;

public interface BugLinkService extends Service {
	public static final String VERSION = "2.2";

	/**
	 * Ask for the list of categories available on this server..
	 * 
	 * @param request
	 * @return
	 */
	ListCategoryResponse listCategories(ListCategoryRequest request);

	/**
	 * Create a new category.
	 * 
	 * @param request
	 * @return
	 */
	CreateCategoryResponse createCategory(CreateCategoryRequest request);

	/**
	 * Update the given category.
	 * 
	 * @param request
	 * @return
	 * @throws RevisionException
	 *             if the request's revision does not match the current revision
	 *             on the server
	 */
	UpdateCategoryResponse updateCategory(UpdateCategoryRequest request)
			throws RevisionException;

	/**
	 * Ask for the list of scan filters available on this server.
	 * 
	 * @param request
	 * @return
	 */
	ListScanFilterResponse listScanFilters(ListScanFilterRequest request);

	/**
	 * Create a new scan filter.
	 * 
	 * @param request
	 * @return
	 */
	CreateScanFilterResponse createScanFilter(CreateScanFilterRequest request);

	/**
	 * Update the given scan filter.
	 * 
	 * @param request
	 * @return
	 * @throws RevisionException
	 *             if the request's revision does not match the current revision
	 *             on the server
	 */
	UpdateScanFilterResponse updateScanFilter(UpdateScanFilterRequest request)
			throws RevisionException;

}

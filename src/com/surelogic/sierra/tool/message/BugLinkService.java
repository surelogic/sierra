package com.surelogic.sierra.tool.message;

import java.io.File;

import com.surelogic.sierra.message.srpc.Service;

@Service(version = "2.2.1")
public interface BugLinkService {

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

	/**
	 * Ask for a list of registered extensions on this server.
	 * 
	 * @param request
	 * @return
	 */
	ListExtensionResponse listExtensions(ListExtensionRequest request);

	/**
	 * Ask for the list of extensions that are actually installed on the server
	 * and available for download.
	 */
	ListExtensionResponse listInstalledExtensions(ListExtensionRequest request);

	/**
	 * Return the metadata for the requested extension.
	 * 
	 * @param name
	 * @param version
	 * @return
	 */
	GetExtensionsResponse getExtensions(GetExtensionsRequest request);

	/**
	 * Download the requested extension.
	 * 
	 * @param name
	 * @param version
	 * @return
	 */
	File downloadExtension(DownloadExtensionRequest request);

	UploadExtensionResponse uploadExtension(UploadExtensionRequest request,
			File file);

	/**
	 * Register an extension on this server.
	 * 
	 * @param request
	 * @return
	 */
	RegisterExtensionResponse registerExtension(RegisterExtensionRequest request);

	/**
	 * Ensure that the provided list of extensions reside on the server. This
	 * check should be made before trying to commit data to the server that is
	 * dependent on these extensions.
	 * 
	 * @param request
	 * @return
	 */
	EnsureExtensionResponse ensureExtensions(EnsureExtensionRequest request);

}

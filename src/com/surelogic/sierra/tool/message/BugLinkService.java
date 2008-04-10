package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.Service;

public interface BugLinkService extends Service {
	public static final String VERSION = "2.2";

	ListCategoryResponse listCategories(ListCategoryRequest request);

	CreateCategoryResponse createCategory(CreateCategoryRequest request);

	UpdateCategoryResponse updateCategory(UpdateCategoryRequest request)
			throws RevisionException;

}

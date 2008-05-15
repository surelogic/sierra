package com.surelogic.sierra.client.eclipse.views;

import com.surelogic.sierra.tool.message.ListCategoryResponse;

class ServerUpdateStatus {
	private final ListCategoryResponse categoriesResponse;
	
	ServerUpdateStatus(ListCategoryResponse sfr) {
		categoriesResponse = sfr;
	}

	int getNumUpdatedFilterSets() {
		if (categoriesResponse == null) {
			return 0;
		}
		return categoriesResponse.getFilterSets().size();
	}
}

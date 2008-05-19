package com.surelogic.sierra.client.eclipse.views;

import com.surelogic.sierra.tool.message.*;

class ServerUpdateStatus {
	private final ListCategoryResponse categoriesResponse;
	private final ListScanFilterResponse filtersResponse;
	
	ServerUpdateStatus(ListCategoryResponse cr, ListScanFilterResponse sfr) {
		categoriesResponse = cr;
		filtersResponse = sfr;
	}

	int getNumUpdatedFilterSets() {
		if (categoriesResponse == null) {
			return 0;
		}
		return categoriesResponse.getFilterSets().size();
	}
	
	int getNumUpdatedScanFilters() {
		if (filtersResponse == null) {
			return 0;
		}
		return filtersResponse.getScanFilter().size();
	}
}

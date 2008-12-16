package com.surelogic.sierra.client.eclipse.views;

import com.surelogic.sierra.jdbc.settings.ServerScanFilterInfo;
import com.surelogic.sierra.tool.message.*;

class ServerUpdateStatus {
	private final ListCategoryResponse categoriesResponse;
	private final ServerScanFilterInfo filtersResponse;
	
	ServerUpdateStatus(ListCategoryResponse cr, ServerScanFilterInfo sfr) {
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
		return filtersResponse.numChanged() + 
		       filtersResponse.getDeletions().size();
	}

	public Iterable<FilterSet> getUpdatedCategories() {
		return categoriesResponse.getFilterSets();
	}
	
	public Iterable<ScanFilter> getScanFilters() {
		return filtersResponse.getScanFilters();
	}
	
	public boolean isChanged(ScanFilter f) {
		return filtersResponse.isChanged(f);
	}
}

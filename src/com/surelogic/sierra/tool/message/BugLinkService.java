package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.Service;

public interface BugLinkService extends Service {
	public static final String VERSION = "2.2";

	ListFilterSetResponse listFilterSets(ListFilterSetRequest request);

	CreateFilterSetResponse createFilterSet(CreateFilterSetRequest request);

	UpdateFilterSetResponse updateFilterSet(UpdateFilterSetRequest request)
			throws RevisionException;

}

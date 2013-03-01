package com.surelogic.sierra.client.eclipse.views.adhoc;

import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.ui.adhoc.views.results.AbstractQueryResultsView;

public final class QueryResultsView extends AbstractQueryResultsView {

	@Override
	public AdHocManager getManager() {
		return SierraDataSource.getManager();
	}
}

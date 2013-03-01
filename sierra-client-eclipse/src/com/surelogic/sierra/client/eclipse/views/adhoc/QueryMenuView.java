package com.surelogic.sierra.client.eclipse.views.adhoc;

import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.ui.adhoc.views.menu.AbstractQueryMenuView;

public final class QueryMenuView extends AbstractQueryMenuView {

	@Override
	public AdHocManager getManager() {
		return SierraDataSource.getManager();
	}
}

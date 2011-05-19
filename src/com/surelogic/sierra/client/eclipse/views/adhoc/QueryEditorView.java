package com.surelogic.sierra.client.eclipse.views.adhoc;

import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.ui.adhoc.views.editor.AbstractQueryEditorView;

public final class QueryEditorView extends AbstractQueryEditorView {

	@Override
	public AdHocManager getManager() {
		return AdHocDataSource.getManager();
	}
}

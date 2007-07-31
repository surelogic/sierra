package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandItem;

public final class FindingsMediator {

	private final Combo f_projectCombo;

	private final Composite f_topSash;

	private final ExpandItem f_detailsItem;

	private final Composite f_detailsComp;

	private final ExpandItem f_commentsItem;

	private final Composite f_commentsComp;

	FindingsMediator(Combo projectCombo, Composite topSash,
			ExpandItem detailsItem, Composite detailsComp,
			ExpandItem commentsItem, Composite commentsComp) {
		f_projectCombo = projectCombo;
		f_topSash = topSash;
		f_detailsItem = detailsItem;
		f_detailsComp = detailsComp;
		f_commentsItem = commentsItem;
		f_commentsComp = commentsComp;
	}

	public void setFocus() {
		// TODO Auto-generated method stub

	}
}

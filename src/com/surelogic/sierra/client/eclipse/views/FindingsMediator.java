package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.ui.PlatformUI;

import com.surelogic.adhoc.views.QueryUtility;
import com.surelogic.sierra.client.eclipse.SLog;
import com.surelogic.sierra.client.eclipse.model.FindingsOrganization;
import com.surelogic.sierra.db.Data;

public final class FindingsMediator {

	private final Combo f_projectCombo;

	private final Composite f_topSash;

	private final ExpandItem f_detailsItem;

	private final Composite f_detailsComp;

	private final ExpandItem f_logItem;

	private final Composite f_logComp;

	FindingsMediator(Combo projectCombo, Composite topSash,
			ExpandItem detailsItem, Composite detailsComp, ExpandItem logItem,
			Composite logComp) {
		f_projectCombo = projectCombo;
		f_topSash = topSash;
		f_detailsItem = detailsItem;
		f_detailsComp = detailsComp;
		f_logItem = logItem;
		f_logComp = logComp;
	}

	public void setFocus() {
		// TODO Auto-generated method stub

	}
}

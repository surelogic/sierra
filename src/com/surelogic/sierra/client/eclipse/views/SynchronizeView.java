package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.surelogic.common.eclipse.TableUtility;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public final class SynchronizeView extends
		AbstractSierraView<SynchronizeMediator> {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.SynchronizeView";

	@Override
	protected SynchronizeMediator createMorePartControls(final Composite parent) {

		/*
		 * Right side shows a list of synchronize events.
		 */
		final Table syncTable = new Table(parent, SWT.FULL_SELECTION);
		syncTable.setHeaderVisible(true);
		syncTable.setLinesVisible(true);
		syncTable.setVisible(true);

		TableColumn column = new TableColumn(syncTable, SWT.RIGHT);
		column.setText("Occurred");
		column.addListener(SWT.Selection,
				TableUtility.SORT_COLUMN_ALPHABETICALLY);
		column.setMoveable(true);

		column = new TableColumn(syncTable, SWT.NONE);
		column.setText("Server");
		column.addListener(SWT.Selection,
				TableUtility.SORT_COLUMN_ALPHABETICALLY);
		column.setMoveable(true);

		column = new TableColumn(syncTable, SWT.NONE);
		column.setText("Summary");
		column.addListener(SWT.Selection,
				TableUtility.SORT_COLUMN_ALPHABETICALLY);
		column.setMoveable(true);

		for (TableColumn c : syncTable.getColumns()) {
			c.pack();
		}

		final Action omitEmptyEntriesAction = new Action(
				"Omit Empty Synchronize Events", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				f_mediator.setHideEmptyEntries(isChecked());
			}
		};
		omitEmptyEntriesAction.setChecked(PreferenceConstants
				.hideEmptySynchronizeEntries());
		addToViewMenu(omitEmptyEntriesAction);

		return new SynchronizeMediator(this, syncTable);
	}
}

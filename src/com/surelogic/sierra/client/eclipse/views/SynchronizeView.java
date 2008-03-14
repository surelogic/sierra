package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import com.surelogic.adhoc.views.TableUtility;

public final class SynchronizeView extends AbstractSierraView<SynchronizeMediator> {
	public static final String ID = "com.surelogic.sierra.client.eclipse.views.SynchronizeView";

	@Override
	protected SynchronizeMediator createMorePartControls(final Composite parent) {
		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		sash.setLayout(new FillLayout());

		/*
		 * Right side shows a list of synchronize events.
		 */
		final Table syncTable = new Table(sash, SWT.FULL_SELECTION);
		syncTable.setHeaderVisible(true);
		syncTable.setLinesVisible(true);
		syncTable.setVisible(true);

		TableColumn column = new TableColumn(syncTable, SWT.NONE);
		column.setText("Project");
		column.addListener(SWT.Selection, TableUtility.SORT_COLUMN_ALPHABETICALLY);
		column.setMoveable(true);

		column = new TableColumn(syncTable, SWT.NONE);
		column.setText("To Server");
		column.addListener(SWT.Selection, TableUtility.SORT_COLUMN_ALPHABETICALLY);
		column.setMoveable(true);

		column = new TableColumn(syncTable, SWT.RIGHT);
		column.setText("Occurred");
		column.addListener(SWT.Selection, TableUtility.SORT_COLUMN_ALPHABETICALLY);
		column.setMoveable(true);

		for (TableColumn c : syncTable.getColumns()) {
			c.pack();
		}

		/*
		 * Left side shows what came down during that synchronize event.
		 */
		Composite edge = new Composite(sash, SWT.NONE);
		FillLayout layout = new FillLayout();
		layout.marginHeight = 3;
		layout.marginWidth = 3;
		edge.setLayout(layout);
		final Group eventsGroup = new Group(edge, SWT.NONE);
		eventsGroup.setLayout(new FillLayout());

		final Table eventsTable = new Table(eventsGroup, SWT.FULL_SELECTION);
		eventsTable.setHeaderVisible(true);
		eventsTable.setLinesVisible(true);
		eventsTable.setVisible(false);

		column = new TableColumn(eventsTable, SWT.NONE);
		column.setText("User");
		column.addListener(SWT.Selection,
				TableUtility.SORT_COLUMN_ALPHABETICALLY);
		column.setMoveable(true);

		column = new TableColumn(eventsTable, SWT.RIGHT);
		column.setText("Occurred");
		column.addListener(SWT.Selection, TableUtility.SORT_COLUMN_NUMERICALLY);
		column.setMoveable(true);

		column = new TableColumn(eventsTable, SWT.RIGHT);
		column.setText("Finding Id");
		column.addListener(SWT.Selection, TableUtility.SORT_COLUMN_NUMERICALLY);
		column.setMoveable(true);

		column = new TableColumn(eventsTable, SWT.NONE);
		column.setText("Audit");
		column.addListener(SWT.Selection,
				TableUtility.SORT_COLUMN_ALPHABETICALLY);
		column.setMoveable(true);

		for (TableColumn c : eventsTable.getColumns()) {
			c.pack();
		}

		sash.setWeights(new int[] { 40, 60 });

		final Action omitEmptyEntriesAction = 
			new Action("Omit Empty Entries", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				f_mediator.setHideEmptyEntries(isChecked());				
			}
		};
		addToViewMenu(omitEmptyEntriesAction);

		return new SynchronizeMediator(this, syncTable, eventsTable);
	}
}

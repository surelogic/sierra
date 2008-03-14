package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.adhoc.views.TableUtility;
import com.surelogic.sierra.client.eclipse.actions.PreferencesAction;

public final class SynchronizeView extends ViewPart {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.SynchronizeView";

	private SynchronizeMediator f_mediator = null;

	@Override
	public void dispose() {
		if (f_mediator != null)
			f_mediator.dispose();
		super.dispose();
	}

	@Override
	public void createPartControl(final Composite parent) {

		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		sash.setLayout(new FillLayout());

		/*
		 * Right side shows a list of synchronize events.
		 */
		final Table syncTable = new Table(sash, SWT.FULL_SELECTION);
		syncTable.setHeaderVisible(true);
		syncTable.setLinesVisible(true);

		/*
		 * TODO: The sorts don't work because the data used to query the details
		 * about the sync with server are lost during the sort. This needs to be
		 * fixed.
		 */
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

		/*
		 * Allow direct access to the preferences from the view.
		 */
		final IMenuManager menu = getViewSite().getActionBars()
				.getMenuManager();
		final Action omitEmptyEntriesAction = 
			new Action("Omit Empty Entries", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				f_mediator.setHideEmptyEntries(isChecked());				
			}
		};
		menu.add(omitEmptyEntriesAction);
		menu.add(new Separator());
		menu.add(new PreferencesAction("Preferences..."));
		
		/*
		 * Allow access to help via the F1 key.
		 */
		getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp(
				parent,
				"com.surelogic.sierra.client.eclipse.view-synchronize-history");

		f_mediator = new SynchronizeMediator(syncTable, eventsTable);
		f_mediator.init();
	}

	@Override
	public void setFocus() {
		if (f_mediator != null)
			f_mediator.setFocus();
	}
}

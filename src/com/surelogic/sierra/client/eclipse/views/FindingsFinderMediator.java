package com.surelogic.sierra.client.eclipse.views;

import java.util.logging.Level;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionFilterFactory;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;
import com.surelogic.sierra.client.eclipse.views.FilterSelectionMenu.ISelectionMenuObserver;

public final class FindingsFinderMediator implements IProjectsObserver,
		ISelectionObserver, ISelectionMenuObserver {

	private final PageBook f_pages;
	private final Control f_noFindingsPage;
	private final Control f_findingsPage;
	private final CascadingList f_finder;
	private final ToolItem f_clearSelectionItem;
	private final Link f_breadcrumbs;
	private final Link f_savedSelections;

	private final SelectionManager f_manager = SelectionManager.getInstance();

	private Selection f_workingSelection;

	FindingsFinderMediator(PageBook pages, Control noFindingsPage,
			Control findingsPage, CascadingList finder,
			ToolItem clearSelectionItem, Link breadcrumbs, Link savedSelections) {
		f_pages = pages;
		f_noFindingsPage = noFindingsPage;
		f_findingsPage = findingsPage;
		f_finder = finder;
		f_clearSelectionItem = clearSelectionItem;
		f_breadcrumbs = breadcrumbs;
		f_savedSelections = savedSelections;
	}

	public void init() {
		f_savedSelections.setText("Saved selections:");

		f_clearSelectionItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				reset();
			}
		});

		f_breadcrumbs.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final int column = Integer.parseInt(event.text);
				System.out.println("TODO: jump back to column " + column);
				emptyAfter(column);
			}
		});

		Projects.getInstance().addObserver(this);
		notify(Projects.getInstance());
	}

	public void setFocus() {
		f_finder.setFocus();
	}

	public void dispose() {
		// TODO
	}

	public void notify(Projects p) {
		final Control page;
		if (p.isEmpty()) {
			page = f_noFindingsPage;
		} else {
			page = f_findingsPage;
		}
		// beware the thread context this method call might be made in.
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (page != f_pages.getPage()) {
					f_pages.showPage(page);
					reset();
				}
			}
		});
	}

	private void reset() {
		f_breadcrumbs.setText("");
		f_workingSelection = f_manager.construct();
		f_workingSelection.addObserver(this);
		f_finder.addColumnAfter(new CascadingList.IColumn() {
			public void createContents(Composite panel) {
				FilterSelectionMenu menu = new FilterSelectionMenu(
						f_workingSelection.getAvailableFilters(), panel);
				menu.addObserver(FindingsFinderMediator.this);
			}
		}, -1);
	}

	public void filterSelected(ISelectionFilterFactory filter,
			FilterSelectionMenu menu) {
		if (f_workingSelection == null)
			throw new IllegalStateException(
					"null working selection upon cascading list menu selection (bug)");
		menu.setEnabled(false);
		final int column = f_finder.getColumnIndexOf(menu.getPanel());
		/*
		 * Filters start being applied in column 1 of the cascading list. Thus,
		 * we need to subtract one from the cascading list column to get the
		 * column to use to "empty after" the list of filters applied to the
		 * selection.
		 */
		f_workingSelection.emptyAfter(column - 1);
		f_finder.addColumnAfter(new CascadingList.IColumn() {
			public void createContents(Composite panel) {
				final Display display = panel.getShell().getDisplay();
				panel.setBackground(display
						.getSystemColor(SWT.COLOR_LIST_SELECTION));
				final Label waitLabel = new Label(panel, SWT.NONE);
				waitLabel.setText("Please wait...");
				waitLabel.setBackground(display
						.getSystemColor(SWT.COLOR_LIST_SELECTION));
			}
		}, column);
		final Filter newFilter = f_workingSelection.construct(filter);
		newFilter.initAsync(new DrawColumn(column, newFilter, menu));
	}

	class DrawColumn implements Filter.CompletedAction {

		private final int f_column;
		private final Filter f_filter;
		private final FilterSelectionMenu f_menu;

		public DrawColumn(int column, Filter filter, FilterSelectionMenu menu) {
			f_column = column;
			assert filter != null;
			f_filter = filter;
			assert menu != null;
			f_menu = menu;
		}

		public void failure(final Exception e) {
			System.out.println("failure");
			// beware the thread context this method call might be made in.
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					final String msg = "Applying the '"
							+ f_filter.getFactory().getFilterLabel()
							+ "' filter to the current selection failed (bug).";
					ErrorDialog.openError(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(),
							"Selection Error", msg, SLStatus.createErrorStatus(
									"Initialization of the filter failed.", e));
					SLLogger.getLogger().log(Level.SEVERE, msg, e);
					f_menu.setEnabled(true);
				}
			});
		}

		public void success() {
			System.out.println("done with queries!!!");
			// beware the thread context this method call might be made in.
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					constructFilterReport(f_column, f_filter);
					f_menu.setEnabled(true);
				}
			});

		}
	}

	public void selectionStructureChanged(Selection selection) {
		final StringBuilder b = new StringBuilder();
		String lastName = null;
		int column = 1;
		for (Filter filter : selection.getFilters()) {
			final String name = filter.getFactory().getFilterLabel();
			if (lastName == null) {
				lastName = name;
			} else {
				b.append("<a href=\"").append(column++).append("\">");
				b.append(lastName).append("</a>");
				b.append(" | ");
				lastName = name;
			}
		}
		if (lastName != null) {
			b.append(lastName);
		}
		f_breadcrumbs.setText(b.toString());
	}

	private void constructFilterReport(final int column, final Filter filter) {
		Object data = new FilterSelectionReport(f_finder, column, filter, this);
		f_finder.setColumnData(column + 1, data);
	}

	void emptyAfter(final int column) {
		f_finder.emptyAfter(column);
		/*
		 * Filters start being applied in column 1 of the cascading list. Thus,
		 * we need to subtract one from the cascading list column to get the
		 * column to use to "empty after" the list of filters applied to the
		 * selection.
		 */
		f_workingSelection.emptyAfter(column - 1);
		Object data = f_finder.getColumnData(column);
		if (data instanceof FilterSelectionReport) {
			FilterSelectionReport report = (FilterSelectionReport) data;
			if (report.hasAMenu())
				report.getMenu().clearSelection();
		}
	}
}

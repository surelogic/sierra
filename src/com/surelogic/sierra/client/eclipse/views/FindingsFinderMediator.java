package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionFilterFactory;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;
import com.surelogic.sierra.client.eclipse.views.FilterSelectionMenu.ISelectionObserver;

public final class FindingsFinderMediator implements IProjectsObserver,
		ISelectionObserver {

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

		Projects.getInstance().addObserver(this);
		notify(Projects.getInstance());
	}

	public void setFocus() {
		f_finder.setFocus();
	}

	public void dispose() {
		// TODO Auto-generated method stub
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
		f_finder.addColumnAfter(new CascadingList.IColumn() {
			public void createContents(Composite panel, int index) {
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
		final int column = f_finder.getColumnIndexOf(menu.getPanel());
		/*
		 * Filters start being applied in column 1 of the cascading list. Thus,
		 * we need to subtract one from the cascading list column to get the
		 * column to use to "empty after" the list of filters applied to the
		 * selection.
		 */
		f_workingSelection.emptyAfter(column - 1);
		f_finder.addColumnAfter(new CascadingList.IColumn() {
			public void createContents(Composite panel, int index) {
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
		newFilter.initAsync(new DrawColumn(column, newFilter,
				f_workingSelection));
	}

	static class DrawColumn implements Runnable {

		private final int f_column;
		private final Filter f_filter;
		private final Selection f_selection;

		public DrawColumn(int column, Filter filter, Selection selection) {
			f_column = column;
			assert filter != null;
			f_filter = filter;
			assert selection != null;
			f_selection = selection;
		}

		public void run() {
			System.out.println("done with queries!!!");

		}
	}
}

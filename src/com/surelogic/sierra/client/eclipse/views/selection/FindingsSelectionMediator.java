package com.surelogic.sierra.client.eclipse.views.selection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

public final class FindingsSelectionMediator implements IProjectsObserver,
		CascadingList.ICascadingListObserver {

	private final PageBook f_pages;
	private final Control f_noFindingsPage;
	private final Control f_findingsPage;
	private final CascadingList f_cascadingList;
	private final ToolItem f_clearSelectionItem;
	private final Link f_breadcrumbs;
	private final Link f_savedSelections;

	private final SelectionManager f_manager = SelectionManager.getInstance();

	private Selection f_workingSelection = null;

	private MColumn f_first = null;

	FindingsSelectionMediator(PageBook pages, Control noFindingsPage,
			Control findingsPage, CascadingList cascadingList,
			ToolItem clearSelectionItem, Link breadcrumbs, Link savedSelections) {
		f_pages = pages;
		f_noFindingsPage = noFindingsPage;
		f_findingsPage = findingsPage;
		f_cascadingList = cascadingList;
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
				if (event.text.equals("save")) {
					/*
					 * TODO: save the current selection
					 */
				} else {
					final int column = Integer.parseInt(event.text);
					emptyAfter(column);
				}
			}
		});

		f_cascadingList.addObserver(this);
		Projects.getInstance().addObserver(this);
		notify(Projects.getInstance());
	}

	public void setFocus() {
		f_cascadingList.setFocus();
	}

	public void dispose() {
		f_cascadingList.removeObserver(this);
		Projects.getInstance().removeObserver(this);
	}

	public void notify(Projects p) {
		/*
		 * We are checking if there is anything in the database at all. If not
		 * we show a helpful message, if so we display the findings selection
		 * page.
		 */
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
		if (f_first != null)
			f_first.dispose();
		f_breadcrumbs.setText("");
		// f_cascadingList.empty();
		f_workingSelection = f_manager.construct();
		f_first = new MRadioMenuColumn(f_cascadingList, f_workingSelection,
				null);
		f_first.init();
	}

	public void notify(CascadingList cascadingList) {
		updateBreadcrumbs();
	}

	private void updateBreadcrumbs() {
		final StringBuilder b = new StringBuilder();
		int column = 1;
		boolean first = true;
		MColumn clColumn = f_first;
		do {
			if (clColumn instanceof MFilterSelectionColumn) {
				MFilterSelectionColumn fsc = (MFilterSelectionColumn) clColumn;
				final Filter filter = fsc.getFilter();
				final String name = filter.getFactory().getFilterLabel();
				if (first) {
					first = false;
				} else {
					b.append(" | ");
				}
				if (filter.isLastFilter() && !showingFindings(fsc)) {
					b.append(name);
				} else {
					b.append("<a href=\"").append(column++).append("\">");
					b.append(name).append("</a>");
				}
			} else if (clColumn instanceof MListOfFindingsColumn) {
				b.append(" | Show");
			}
			clColumn = clColumn.getNextColumn();
		} while (clColumn != null);
		f_breadcrumbs.setText(b.toString());
		f_breadcrumbs.getParent().layout();
	}

	private boolean showingFindings(final MColumn column) {
		if (column instanceof MListOfFindingsColumn)
			return true;
		if (column.hasNextColumn())
			return showingFindings(column.getNextColumn());
		else
			return false;

	}

	public void selectionChanged(Selection selecton) {
		/*
		 * Nothing to do.
		 */
	}

	void emptyAfter(final int column) {
		final int after = column - 1;
		final int filterCount = f_workingSelection.getFilterCount();
		MColumn col;
		if (filterCount != column) {
			f_workingSelection.emptyAfter(after);
		} else {
			// remove the last column of the viewer
			col = f_first;
			while (col.hasNextColumn())
				col = col.getNextColumn();
			col.dispose();
		}
		/*
		 * We need to clear the menu choice.
		 */
		col = f_first;
		while (col.hasNextColumn())
			col = col.getNextColumn();
		if (col instanceof MRadioMenuColumn) {
			((MRadioMenuColumn) col).clearMenuSelection();
		}
	}
}

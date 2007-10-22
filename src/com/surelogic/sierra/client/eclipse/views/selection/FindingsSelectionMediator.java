package com.surelogic.sierra.client.eclipse.views.selection;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.sierra.client.eclipse.dialogs.NameSavedSelectionDialong;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionManagerObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

public final class FindingsSelectionMediator implements IProjectsObserver,
		CascadingList.ICascadingListObserver, ISelectionManagerObserver {

	private static final String SAVE_LINK = "save";

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
		f_clearSelectionItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				reset();
			}
		});

		f_breadcrumbs.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final int column = Integer.parseInt(event.text);
				emptyAfter(column);
			}
		});

		f_savedSelections.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final String selectionName = event.text;
				if (SAVE_LINK.equals(selectionName)) {
					/*
					 * Save the current selection.
					 */
					NameSavedSelectionDialong dialog = new NameSavedSelectionDialong(
							f_cascadingList.getShell());
					if (Window.CANCEL != dialog.open()) {
						/*
						 * Save the selection
						 */
						String name = dialog.getName();
						if (name == null)
							return;
						name = name.trim();
						if ("".equals(name))
							return;
						f_manager.saveSelection(name, new Selection(
								f_workingSelection));
					}
				} else {
					/*
					 * Load the current selection.
					 */
					final Selection selection = f_manager
							.getSavedSelection(selectionName);
					if (selection == null)
						return;

					reset();
					f_workingSelection = new Selection(selection);
					f_workingSelection.changed();
					boolean first = true;
					MColumn prev = f_first;
					MRadioMenuColumn menu;
					int afterCol = 0;
					for (Filter filter : f_workingSelection.getFilters()) {
						if (first) {
							first = false;
							/*
							 * Set the first menu to the right selection.
							 */
							menu = (MRadioMenuColumn) f_first;
						} else {
							/*
							 * Create a menu
							 */
							menu = new MRadioMenuColumn(f_cascadingList,
									f_workingSelection, prev);
							menu.init();
							afterCol++;
						}
						menu.setSelection(filter.getFactory().getFilterLabel());
						prev = menu;

						MFilterSelectionColumn fCol = new MFilterSelectionColumn(
								f_cascadingList, f_workingSelection, prev,
								afterCol++, filter);
						fCol.init();
						prev = fCol;
					}
				}
			}
		});

		f_cascadingList.addObserver(this);
		f_manager.addObserver(this);
		Projects.getInstance().addObserver(this);
		notify(Projects.getInstance());
		updateSavedSelections();
	}

	public void setFocus() {
		f_cascadingList.setFocus();
	}

	public void dispose() {
		f_cascadingList.removeObserver(this);
		f_manager.removeObserver(this);
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
		f_workingSelection = f_manager.construct();
		updateSavedSelections();
		f_first = new MRadioMenuColumn(f_cascadingList, f_workingSelection,
				null);
		f_first.init();
	}

	public void notify(CascadingList cascadingList) {
		updateBreadcrumbs();
		updateSavedSelections();
	}

	public void savedSelectionsChanged(SelectionManager manager) {
		updateSavedSelections();
	}

	private void updateBreadcrumbs() {
		final StringBuilder b = new StringBuilder();
		int column = 0;
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
				final boolean lastFilter = filter.isLastFilter();
				final boolean showingFindings = showingFindings(fsc);
				if (lastFilter && !showingFindings) {
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

	private void updateSavedSelections() {
		StringBuilder b = new StringBuilder();
		final boolean saveable = f_workingSelection != null
				&& f_workingSelection.getFilterCount() > 0;
		final boolean hasSavedSelections = !f_manager.isEmpty();

		if (hasSavedSelections) {
			if (saveable) {
				b.append("<a href=\"");
				b.append(SAVE_LINK);
				b.append("\">Save</a>d Selections:");
			} else {
				b.append("Saved Selections:");
			}

			for (String link : f_manager.getSavedSelectionNames()) {
				b.append(" <a href=\"");
				b.append(link);
				b.append("\">");
				b.append(link);
				b.append("</a>");
			}
		} else {
			if (saveable) {
				b.append("<a href=\"");
				b.append(SAVE_LINK);
				b.append("\">Save Selection</a>");
			} else {
				b.append("(no saved selections)");
			}
		}
		f_savedSelections.setText(b.toString());
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
		final int filterCount = f_workingSelection.getFilterCount();
		MColumn col;
		if (filterCount != column + 1) {
			f_workingSelection.emptyAfter(column);
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

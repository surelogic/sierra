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
import com.surelogic.common.eclipse.RadioArrowMenu;
import com.surelogic.common.eclipse.RadioArrowMenu.IRadioMenuObserver;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.AbstractFilterObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionFilterFactory;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

public final class FindingsFinderMediator implements IProjectsObserver,
		ISelectionObserver, IRadioMenuObserver {

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
		f_breadcrumbs.setText("");
		f_finder.empty();
		f_workingSelection = f_manager.construct();
		f_workingSelection.addObserver(this);
		addMenu(null);
	}

	private void addMenu(final Filter input) {
		final CascadingList.IColumn m = new CascadingList.IColumn() {
			public void createContents(Composite panel) {
				final RadioArrowMenu menu = new RadioArrowMenu(panel);
				if (input != null) {
					menu.addChoice("Show", null);
					menu.addSeparator();
					input.addObserver(new AbstractFilterObserver() {
						@Override
						public void porous(final Filter filter) {
							f_finder.getDisplay().asyncExec(new Runnable() {
								public void run() {
									// menu.setEnabled(filter.isPorous());
								}
							});
						}
					});
				}
				for (ISelectionFilterFactory f : f_workingSelection
						.getAvailableFilters()) {
					menu.addChoice(f, null);
				}
				menu.addObserver(FindingsFinderMediator.this);
			}
		};
		f_finder.addColumn(m, true);
	}

	public void selected(Object choice, RadioArrowMenu menu) {
		if (f_workingSelection == null)
			throw new IllegalStateException(
					"null working selection upon cascading list menu selection (bug)");
		/*
		 * Filters start being applied in column 1 of the cascading list. Thus,
		 * we need to subtract one from the cascading list column to get the
		 * column to use to "empty after" the list of filters applied to the
		 * selection.
		 */
		final int column = f_finder.getColumnIndexOf(menu.getPanel());
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
		}, column, false);

		final int selectionIndex = (column / 2) - 1;
		f_workingSelection.emptyAfter(selectionIndex);

		// menu.setEnabled(false);
		//System.out.println("selected: emptyAfter=" + selectionIndex);
		//System.out.println("selected: addColumnAfter=" + column);

		if (choice instanceof ISelectionFilterFactory) {
			final ISelectionFilterFactory filter = (ISelectionFilterFactory) choice;
			f_workingSelection.construct(filter, new DrawFilterAndMenu(column,
					menu));
		} else if (choice.equals("Show")) {
			//System.out.println("show");
			final FindingsSelectionReport fsr = new FindingsSelectionReport(
					f_workingSelection, f_finder, column, f_manager
							.getExecutor());
			fsr.init();
		}
	}

	class DrawFilterAndMenu extends AbstractFilterObserver {

		private final int f_waitMsgColumn;
		private final RadioArrowMenu f_selectingMenu;

		public DrawFilterAndMenu(int waitMsgColumn, RadioArrowMenu selectingMenu) {
			f_waitMsgColumn = waitMsgColumn;
			assert selectingMenu != null;
			f_selectingMenu = selectingMenu;
		}

		@Override
		public void queryFailure(final Filter filter, final Exception e) {
			//System.out.println("failure");
			// beware the thread context this method call might be made in.
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					final String msg = "Applying the '"
							+ filter.getFactory().getFilterLabel()
							+ "' filter to the current selection failed (bug).";
					ErrorDialog.openError(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(),
							"Selection Error", msg, SLStatus.createErrorStatus(
									"Initialization of the filter failed.", e));
					SLLogger.getLogger().log(Level.SEVERE, msg, e);
					f_selectingMenu.setEnabled(true);
				}
			});
			filter.removeObserver(this);
		}

		@Override
		public void contentsChanged(final Filter filter) {
			//System.out.println("contentsChanged " + filter + " " + this);
			constructFilterReport(filter);
		}

		@Override
		public void contentsEmpty(Filter filter) {
			constructFilterReport(filter);
		}

		private void constructFilterReport(final Filter filter) {
			// beware the thread context this method call might be made in.
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					new FilterSelectionReport(f_finder, f_waitMsgColumn, filter);
					addMenu(filter);
					f_selectingMenu.setEnabled(true);
				}
			});
			filter.removeObserver(this);
		}

		@Override
		public void dispose(Filter filter) {
			SLLogger.getLogger().log(
					Level.SEVERE,
					"Unexpected dispose() callback from " + filter
							+ " while it was being created (bug)");
			filter.removeObserver(this);
		}

		@Override
		public String toString() {
			return "[DrawFilterAndMenu waitMsgColumn=" + f_waitMsgColumn + "]";
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
		f_breadcrumbs.getParent().layout();
	}

	public void selectionChanged(Selection selecton) {
		/*
		 * Nothing to do.
		 */
	}

	void emptyAfter(final int column) {
		final int finderColumn = column * 2;
		f_finder.emptyAfter(finderColumn);
		/*
		 * Filters start being applied in column 1 of the cascading list. Thus,
		 * we need to subtract one from the cascading list column to get the
		 * column to use to "empty after" the list of filters applied to the
		 * selection.
		 */
		f_workingSelection.emptyAfter(column - 1);
	}
}

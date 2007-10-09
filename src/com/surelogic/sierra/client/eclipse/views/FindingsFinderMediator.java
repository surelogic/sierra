package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

import com.surelogic.adhoc.views.QueryUtility;
import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.RadioArrowMenu;
import com.surelogic.common.eclipse.CascadingList.IColumn;
import com.surelogic.common.eclipse.RadioArrowMenu.IRadioMenuObserver;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
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
		// TODO
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
				for (ISelectionFilterFactory f : f_workingSelection
						.getAvailableFilters()) {
					menu.addChoice(f, null);
				}
				if (input != null) {
					menu.addSeparator();
					menu.addChoice("Show", null);
					menu.addChoice("Graph", null);
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
		 * Filters start being applied in column 1 of the cascading list.
		 * Thus, we need to subtract one from the cascading list column to
		 * get the column to use to "empty after" the list of filters
		 * applied to the selection.
		 */
		final int column = f_finder.getColumnIndexOf(menu.getPanel());
		final int selectionIndex = (column / 2) - 1;
		f_workingSelection.emptyAfter(selectionIndex);
		if (choice instanceof ISelectionFilterFactory) {
			final ISelectionFilterFactory filter = (ISelectionFilterFactory) choice;
			menu.setEnabled(false);
			System.out.println("selected: emptyAfter=" + selectionIndex);
			System.out.println("selected: addColumnAfter=" + column);
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
			f_workingSelection.construct(filter, new DrawFilterAndMenu(column,
					menu));
		} else if (choice.equals("Show")) {
			System.out.println("show");
			final String query = f_workingSelection.getQuery();

			try {
				final Connection c = Data.getConnection();
				try {
					final Statement st = c.createStatement();
					try {
						System.out.println(query);
						final ResultSet rs = st.executeQuery(query);
						f_finder.addColumnAfter(new IColumn() {
							public void createContents(Composite panel) {
								try {
									QueryUtility.construct(panel, QueryUtility
											.getColumnLabels(rs), QueryUtility
											.getRows(rs, 1000));
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}, column, false);

					} finally {
						st.close();
					}
				} finally {
					c.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			System.out.println("failure");
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
			System.out.println("contentsChanged " + filter + " " + this);
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

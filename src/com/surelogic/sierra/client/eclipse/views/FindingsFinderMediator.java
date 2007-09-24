package com.surelogic.sierra.client.eclipse.views;

import java.util.Map;
import java.util.logging.Level;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.StringUtility;
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

	private final Color f_slBlue;
	private final Color f_slOrange;

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

		final Display display = f_pages.getDisplay();
		f_slBlue = new Color(display, 43, 97, 153);
		f_slOrange = new Color(display, 218, 127, 48);
	}

	public void init() {
		f_savedSelections.setText("Saved selections:");

		f_clearSelectionItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				reset();
			}
		});

		Projects.getInstance().addObserver(this);
		notify(Projects.getInstance());
	}

	public void setFocus() {
		f_finder.setFocus();
	}

	public void dispose() {
		f_slBlue.dispose();
		f_slOrange.dispose();
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
		int column = 0;
		for (Filter filter : selection.getFilters()) {
			final String name = filter.getFactory().getFilterLabel();
			if (lastName == null) {
				lastName = name;
			} else {
				b.append("<a href=\"").append(column++).append("\">");
				b.append(lastName).append("</a>");
				b.append(" &gt; ");
				lastName = name;
			}
		}
		if (lastName != null) {
			b.append(lastName);
		}
		f_breadcrumbs.setText(b.toString());
	}

	private void constructFilterReport(final int column, final Filter filter) {
		CascadingList.IColumn c = new CascadingList.IColumn() {
			public void createContents(Composite panel) {
				boolean showAMenu = filter.getSelection().getAvailableFilters()
						.size() > 0;
				GridLayout gridLayout = new GridLayout();
				if (showAMenu)
					gridLayout.numColumns = 2;
				panel.setLayout(gridLayout);
				Group group = new Group(panel, SWT.NONE);
				group.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP, false,
						false));
				group.setText(filter.getFactory().getFilterLabel());
				RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
				rowLayout.fill = true;
				rowLayout.wrap = false;
				group.setLayout(rowLayout);
				Label lt = new Label(group, SWT.RIGHT);
				lt.setText("Findings");
				final int total = filter.getFindingCountTotal();
				for (Map.Entry<String, Integer> count : filter
						.getSummaryCounts().entrySet()) {
					newReport(group, count.getKey(), null, count.getValue(),
							total);
				}
				Label st = new Label(group, SWT.RIGHT);
				st.setText(StringUtility.toCommaSepString(total));

				final Link li = new Link(group, SWT.WRAP);
				li
						.setText("<A HREF=\"select\">Graph</A> <A HREF=\"deselect\">Show</A>");
				group.pack();

				if (showAMenu) {
					Composite rhs = new Composite(panel, SWT.NONE);
					rhs.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP, false,
							false));
					FilterSelectionMenu menu = new FilterSelectionMenu(filter
							.getSelection().getAvailableFilters(), rhs);
					menu.addObserver(FindingsFinderMediator.this);
				}
			}
		};
		f_finder.addColumnAfter(c, column);
	}

	private void newReport(Composite parent, String text, Image image,
			final int value, final int total) {
		final Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		result.setLayout(layout);

		final Button b = new Button(result, SWT.CHECK);
		b.setText(text);
		b.setImage(image);
		final Point bSize = b.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Canvas c = new Canvas(result, SWT.NONE) {
			@Override
			public Point computeSize(int hint, int hint2, boolean changed) {
				return new Point(75, bSize.y);
			}
		};
		c.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		c.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				final Display display = result.getDisplay();
				Point cSize = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				GC gc = e.gc;
				Color foreground = gc.getForeground();
				Color background = gc.getBackground();
				gc.setForeground(f_slOrange);
				gc.setBackground(f_slBlue);
				int percent = (int) (((double) value / (double) total) * 100);
				int width = (cSize.x - 1) * percent / 100;
				if (width < 2)
					width = 2;
				gc.fillGradientRectangle(0, 0, width, cSize.y, true);
				Rectangle rect2 = new Rectangle(0, 0, cSize.x - 1, cSize.y - 1);
				gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
				gc.drawRectangle(rect2);
				if (percent > 25) {
					int p = (cSize.x - 1) * 25 / 100;
					gc.drawLine(p, 0, p, cSize.y - 1);
				}
				if (percent > 50) {
					int p = (cSize.x - 1) * 50 / 100;
					gc.drawLine(p, 0, p, cSize.y - 1);
				}
				if (percent > 75) {
					int p = (cSize.x - 1) * 75 / 100;
					gc.drawLine(p, 0, p, cSize.y - 1);
				}
				String text = StringUtility.toCommaSepString(value);
				Point size = e.gc.textExtent(text);
				int offset = Math.max(0, (cSize.y - size.y) / 2);
				int rightJ = cSize.x - 2 - size.x;
				gc.drawText(text, rightJ, 0 + offset, true);
				gc.setForeground(background);
				gc.setBackground(foreground);
			}
		});
	}
}

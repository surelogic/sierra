package com.surelogic.sierra.client.eclipse.views.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.StringUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.IFilterObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;

public final class MFilterSelectionColumn extends MColumn implements
		IFilterObserver, FilterSelectionReportLine.ISelectionChangedObserver {

	private final int f_addAfterColumn;

	private int f_column = -1;

	private final Filter f_filter;

	Filter getFilter() {
		return f_filter;
	}

	private Label f_totalCount = null;
	private Label f_porousCount = null;
	private Group f_reportGroup = null;
	private Composite f_panel = null;

	private Menu f_menu = null;
	private MenuItem f_selectAllMenuItem = null;
	private MenuItem f_deselectAllMenuItem = null;
	private MenuItem f_sortByCountMenuItem = null;

	private final List<FilterSelectionReportLine> f_lines = new ArrayList<FilterSelectionReportLine>();

	private boolean f_sortByCount = false;

	MFilterSelectionColumn(CascadingList cascadingList, Selection selection,
			MColumn previousColumn, int addAfterColumn, Filter filter) {
		super(cascadingList, selection, previousColumn);
		f_addAfterColumn = addAfterColumn;
		assert filter != null;
		f_filter = filter;
	}

	@Override
	void init() {
		CascadingList.IScrolledColumn c = new CascadingList.IScrolledColumn() {
			public void createContents(Composite panel) {
				f_panel = panel;
				f_reportGroup = new Group(panel, SWT.NONE);
				f_reportGroup.setText(f_filter.getFactory().getFilterLabel());
				RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
				rowLayout.fill = true;
				rowLayout.wrap = false;
				f_reportGroup.setLayout(rowLayout);

				f_totalCount = new Label(f_reportGroup, SWT.RIGHT);

				f_menu = new Menu(f_panel.getShell(), SWT.POP_UP);
				f_menu.addListener(SWT.Show, new Listener() {
					public void handleEvent(Event event) {
						final boolean valuesExist = f_filter.hasValues();
						f_selectAllMenuItem.setEnabled(valuesExist);
						f_deselectAllMenuItem.setEnabled(valuesExist);
						f_sortByCountMenuItem.setSelection(f_sortByCount);
					}
				});

				f_selectAllMenuItem = new MenuItem(f_menu, SWT.PUSH);
				f_selectAllMenuItem.setText("Select All");
				f_selectAllMenuItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						f_filter.setPorousAll();
					}
				});
				f_deselectAllMenuItem = new MenuItem(f_menu, SWT.PUSH);
				f_deselectAllMenuItem.setText("Deselect All");
				f_deselectAllMenuItem.addListener(SWT.Selection,
						new Listener() {
							public void handleEvent(Event event) {
								f_filter.setPorousNone();
							}
						});
				new MenuItem(f_menu, SWT.SEPARATOR);
				f_sortByCountMenuItem = new MenuItem(f_menu, SWT.CHECK);
				f_sortByCountMenuItem.setText("Sort By Finding Count");
				f_sortByCountMenuItem.addListener(SWT.Selection,
						new Listener() {
							public void handleEvent(Event event) {
								f_sortByCount = !f_sortByCount;
								updateReport();
							}
						});

				f_reportGroup.setMenu(f_menu);
				f_totalCount.setMenu(f_menu);

				updateReport();
			}
		};
		f_column = getCascadingList()
				.addColumnAfter(c, f_addAfterColumn, false);
		f_filter.addObserver(this);
		initOfNextColumnComplete();
		/*
		 * Add the radio menu after this item.
		 */
		final MRadioMenuColumn rmc = new MRadioMenuColumn(getCascadingList(),
				getSelection(), this);
		rmc.init();
	}

	@Override
	void dispose() {
		super.dispose();
		f_filter.removeObserver(this);
		if (f_column != -1)
			getCascadingList().emptyFrom(f_column);
	}

	/**
	 * Must be called from the UI thread.
	 */
	private void updateReport() {
		if (f_panel.isDisposed())
			return;
		/*
		 * Fix total count at the top.
		 */
		final int total = f_filter.getFindingCountTotal();
		f_totalCount.setText(StringUtility.toCommaSepString(total)
				+ (total == 1 ? " Finding" : " Findings"));

		/*
		 * Fix the value lines.
		 */
		final List<String> valueList = f_sortByCount ? f_filter
				.getValuesOrderedBySummaryCount() : f_filter.getAllValues();

		int currentIndex = 0;
		for (String value : valueList) {
			final int count = f_filter.getSummaryCountFor(value);

			FilterSelectionReportLine fsrLine;
			if (f_lines.size() > currentIndex) {
				fsrLine = f_lines.get(currentIndex);
				fsrLine.setText(value);
				fsrLine.setCount(count);
				fsrLine.setTotal(total);
			} else {
				fsrLine = new FilterSelectionReportLine(f_reportGroup, value,
						null, count, total);
				fsrLine.setMenu(f_menu);
				fsrLine.addObserver(this);
				f_lines.add(fsrLine);
			}
			fsrLine.setSelection(f_filter.isPorous(value));
			currentIndex++;
		}
		/*
		 * Remove all the extra lines.
		 */
		List<FilterSelectionReportLine> extras = new ArrayList<FilterSelectionReportLine>();
		while (currentIndex < f_lines.size()) {
			extras.add(f_lines.get(currentIndex++));
		}
		f_lines.removeAll(extras);
		for (FilterSelectionReportLine line : extras) {
			/*
			 * We have to set the menu to null before we dispose of the line
			 * because, by default, SWT will dispose the menu of a control that
			 * is being disposed.
			 */
			line.setMenu(null);
			line.dispose();
		}

		final int porousCount = f_filter.getFindingCountPorous();
		if (f_porousCount != null && !f_porousCount.isDisposed())
			f_porousCount.dispose();
		if (!f_lines.isEmpty()) {
			f_porousCount = new Label(f_reportGroup, SWT.RIGHT);
			final String porousCountString = StringUtility
					.toCommaSepString(porousCount);
			f_porousCount.setText(porousCountString);
			f_totalCount.setToolTipText(porousCountString
					+ (porousCount > 1 ? " findings" : " finding")
					+ " selected");
		}
		f_panel.pack();
		f_reportGroup.layout();
	}

	public void porous(Filter filter) {
		if (f_panel.isDisposed())
			return;
		getCascadingList().getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateReport();
			}
		});
	}

	public void contentsChanged(Filter filter) {
		if (f_panel.isDisposed())
			return;
		getCascadingList().getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateReport();
			}
		});
	}

	public void queryFailure(Filter filter, Exception e) {
		// TODO Something more reasonable...not sure what
		SLLogger.getLogger().log(Level.SEVERE, "query failed on " + filter, e);
	}

	public void contentsEmpty(Filter filter) {
		if (f_panel.isDisposed())
			return;
		getCascadingList().getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateReport();
			}
		});
	}

	public void dispose(Filter filter) {
		dispose();
	}

	public void selectionChanged(FilterSelectionReportLine line) {
		/*
		 * The selection changed on a line.
		 */
		f_filter.setPorous(line.getText(), line.getSelection());
	}
}

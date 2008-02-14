package com.surelogic.sierra.client.eclipse.views.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.StringUtility;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.IFilterObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;

public final class MFilterSelectionColumn extends MColumn implements
		IFilterObserver, FilterSelectionReportLine.ISelectionChangedObserver {

	private final Filter f_filter;

	Filter getFilter() {
		return f_filter;
	}

	private Composite f_panel = null;
	private Composite f_reportContents = null;
	private Label f_totalCount = null;
	private Label f_porousCount = null;
	private Group f_reportGroup = null;
	private ScrolledComposite f_reportViewport = null;

	private Menu f_menu = null;
	private MenuItem f_selectAllMenuItem = null;
	private MenuItem f_deselectAllMenuItem = null;
	private MenuItem f_sortByCountMenuItem = null;

	private FilterSelectionReportLine.Factory lineFactory = null;
	private final List<FilterSelectionReportLine> f_lines = new ArrayList<FilterSelectionReportLine>();

	private boolean f_sortByCount = false;

	MFilterSelectionColumn(CascadingList cascadingList, Selection selection,
			MColumn previousColumn, Filter filter) {
		super(cascadingList, selection, previousColumn);
		assert filter != null;
		f_filter = filter;
	}

	@Override
	void init() {
		CascadingList.IColumn c = new CascadingList.IColumn() {
			public Composite createContents(Composite panel) {
				f_panel = new Composite(panel, SWT.NONE);
				f_panel.setLayout(new FillLayout());
				f_reportGroup = new Group(f_panel, SWT.NONE);
				f_reportGroup.setText(f_filter.getFactory().getFilterLabel());
				GridLayout gridLayout = new GridLayout();
				f_reportGroup.setLayout(gridLayout);

				f_totalCount = new Label(f_reportGroup, SWT.RIGHT);
				f_totalCount.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
						true, false));

				f_reportViewport = new ScrolledComposite(f_reportGroup,
						SWT.V_SCROLL | SWT.BORDER);
				f_reportViewport.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
						true, true));
				f_reportContents = new Composite(f_reportViewport, SWT.NONE);
				RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
				rowLayout.fill = true;
				rowLayout.wrap = false;
				f_reportContents.setLayout(rowLayout);
				
				f_reportViewport.setContent(f_reportContents);

				lineFactory = new FilterSelectionReportLine.Factory(f_reportContents);
				
				f_porousCount = new Label(f_reportGroup, SWT.RIGHT);
				f_porousCount.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
						true, false));

				f_menu = new Menu(f_reportGroup.getShell(), SWT.POP_UP);
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

				f_reportViewport.setMenu(f_menu);
				f_reportContents.setMenu(f_menu);
				f_reportGroup.setMenu(f_menu);
				f_totalCount.setMenu(f_menu);
				f_porousCount.setMenu(f_menu);

				updateReport();
				return f_panel;
			}
		};
		getCascadingList().addColumnAfter(c,
				getPreviousColumn().getColumnIndex(), false);
		f_filter.addObserver(this);
		initOfNextColumnComplete();
	}

	@Override
	void dispose() {
		super.dispose();
		f_filter.removeObserver(this);
		final int column = getColumnIndex();
		if (column != -1)
			getCascadingList().emptyFrom(column);
		
		if (lineFactory != null) {
		  lineFactory.dispose();
		}
	}

	@Override
	int getColumnIndex() {
		if (f_panel.isDisposed())
			return -1;
		else
			return getCascadingList().getColumnIndexOf(f_panel);
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

		/*
		 * filterContentsChanged tracks if the rows in this filter selection
		 * column have changed. We want to avoid a call to pack because the
		 * scroll bar gets moved back up to the top each time this method is
		 * called.
		 */
		boolean filterContentsChanged = false;
		int currentIndex = 0;
		for (String value : valueList) {
			final int count = f_filter.getSummaryCountFor(value);

			FilterSelectionReportLine fsrLine;
			if (f_lines.size() > currentIndex) {
				fsrLine = f_lines.get(currentIndex);
				boolean unchanged = fsrLine.getText().equals(value)
						&& fsrLine.getCount() == count
						&& fsrLine.getTotal() == total;
				if (!unchanged) {
					fsrLine.setText(value);
					fsrLine.setCount(count);
					fsrLine.setTotal(total);
					filterContentsChanged = true;
				}
			} else {
				fsrLine = lineFactory.create(value, null, count, total);
				fsrLine.setMenu(f_menu);
				fsrLine.addObserver(this);
				f_lines.add(fsrLine);
				filterContentsChanged = true;
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
			filterContentsChanged = true;
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
			f_porousCount.setText("");
		if (!f_lines.isEmpty()) {
			final String porousCountString = StringUtility
					.toCommaSepString(porousCount);
			f_porousCount.setText(porousCountString);
			f_totalCount.setToolTipText(porousCountString
					+ (porousCount > 1 ? " findings" : " finding")
					+ " selected");
		}
		f_reportContents.pack();
		if (filterContentsChanged)
			f_panel.pack();
		f_panel.layout();
	}

	public void filterChanged(Filter filter) {
		if (f_panel.isDisposed())
			return;
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				updateReport();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public void filterQueryFailure(Filter filter, Exception e) {
		SLLogger.getLogger().log(
				Level.SEVERE,
				"query for " + this.getClass().getName() + " failed on "
						+ filter, e);
	}

	public void filterDisposed(Filter filter) {
		dispose();
	}

	public void selectionChanged(FilterSelectionReportLine line) {
		/*
		 * The selection changed on a line.
		 */
		f_filter.setPorous(line.getText(), line.getSelection());
	}
}

package com.surelogic.sierra.client.eclipse.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.StringUtility;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.IFilterObserver;

public final class FilterSelectionReport implements IFilterObserver,
		FilterSelectionReportLine.ISelectionObserver {

	private final CascadingList f_finder;
	private final int f_column;
	private final Filter f_filter;

	private Label f_totalCount = null;
	private Label f_porousCount = null;
	private Group f_reportGroup = null;
	private Composite f_panel = null;

	private final List<FilterSelectionReportLine> lines = new ArrayList<FilterSelectionReportLine>();

	private boolean f_sortByCount = false;

	FilterSelectionReport(CascadingList finder, int column, Filter filter) {
		assert finder != null;
		f_finder = finder;
		f_column = column;
		assert filter != null;
		f_filter = filter;
		constructFilterReport();
	}

	private void constructFilterReport() {
		CascadingList.IColumn c = new CascadingList.IColumn() {
			public void createContents(Composite panel) {
				f_panel = panel;
				f_reportGroup = new Group(panel, SWT.NONE);
				f_reportGroup.setText(f_filter.getFactory().getFilterLabel());
				RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
				rowLayout.fill = true;
				rowLayout.wrap = false;
				f_reportGroup.setLayout(rowLayout);

				f_totalCount = new Label(f_reportGroup, SWT.RIGHT);
				updateReport();
			}
		};
		f_finder.addColumnAfter(c, f_column, false);
		f_filter.addObserver(this);
	}

	private void updateReport() {
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
			if (lines.size() > currentIndex) {
				fsrLine = lines.get(currentIndex);
				fsrLine.setText(value);
				fsrLine.setCount(count);
				fsrLine.setTotal(total);
			} else {
				fsrLine = new FilterSelectionReportLine(f_reportGroup, value,
						null, count, total);
				fsrLine.addObserver(this);
				lines.add(fsrLine);
			}
			fsrLine.setSelection(f_filter.isPorous(value));
			currentIndex++;
		}
		/*
		 * Remove all the extra lines.
		 */
		List<FilterSelectionReportLine> extras = new ArrayList<FilterSelectionReportLine>();
		while (currentIndex < lines.size()) {
			extras.add(lines.get(currentIndex++));
		}
		lines.removeAll(extras);
		for (FilterSelectionReportLine line : extras) {
			line.dispose();
		}

		final int porousCount = f_filter.getFindingCountPorous();
		if (f_porousCount != null && !f_porousCount.isDisposed())
			f_porousCount.dispose();
		f_porousCount = new Label(f_reportGroup, SWT.RIGHT);
		f_porousCount.setText(StringUtility.toCommaSepString(porousCount));
		f_panel.pack();
		f_reportGroup.layout();
	}

	public void porous(Filter filter) {
		f_finder.getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateReport();
			}
		});
	}

	public void contentsChanged(Filter filter) {
		f_finder.getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateReport();
			}
		});
	}

	public void queryFailure(Filter filter, Exception e) {
		// TODO Auto-generated method stub

	}

	public void contentsEmpty(Filter filter) {
		f_finder.getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateReport();
			}
		});
	}

	public void dispose(Filter filter) {
		filter.removeObserver(this);
	}

	public void selectionChanged(FilterSelectionReportLine line) {
		/*
		 * The selection changed on a line.
		 */
		f_filter.setPorous(line.getText(), line.getSelection());
	}
}

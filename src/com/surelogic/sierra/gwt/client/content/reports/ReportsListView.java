package com.surelogic.sierra.gwt.client.content.reports;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ItemLabel;
import com.surelogic.sierra.gwt.client.ui.SelectionTracker;

public class ReportsListView extends BlockPanel {
	private final SelectionTracker<ItemLabel<Report>> selectionTracker = new SelectionTracker<ItemLabel<Report>>();
	private final Map<String, ItemLabel<Report>> reportUIMap = new HashMap<String, ItemLabel<Report>>();

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Reports");

	}

	public void updateReports(ReportCache reports) {
		final VerticalPanel contentPanel = getContentPanel();
		contentPanel.clear();
		reportUIMap.clear();
		for (Report report : reports) {
			ItemLabel<Report> reportUI = new ItemLabel<Report>(
					report.getName(), report, selectionTracker,
					new ReportClickListener(report));
			reportUIMap.put(report.getUuid(), reportUI);
			contentPanel.add(reportUI);
		}
	}

	public void setSelection(Report report) {
		if (report == null) {
			selectionTracker.setSelected(null);
		} else {
			ItemLabel<Report> reportEntry = reportUIMap.get(report.getUuid());
			if (reportEntry != null) {
				reportEntry.setSelected(true);
			}
		}
	}

	private class ReportClickListener implements ClickListener {
		private final Report report;

		public ReportClickListener(Report report) {
			super();
			this.report = report;
		}

		public void onClick(Widget sender) {
			new ReportsContext(report).updateContext();
		}

	}
}

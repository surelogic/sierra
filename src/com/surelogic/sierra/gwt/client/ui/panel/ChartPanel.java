package com.surelogic.sierra.gwt.client.ui.panel;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.chart.Chart;
import com.surelogic.sierra.gwt.client.data.ReportSettings;

public class ChartPanel extends BlockPanel {
	private ReportSettings report;
	private Chart chart;

	public ChartPanel(final ReportSettings reportSettings) {
		super();
		setReport(reportSettings);
	}

	@Override
	protected void onInitialize(final VerticalPanel content) {
		// nothing to do
	}

	public void setReport(final ReportSettings r) {
		report = r;
		if (report != null) {
			setTitle(report.getTitle());
			setSummary(report.getDescription());

			final VerticalPanel content = getContentPanel();
			if (chart != null) {
				content.remove(chart);
			}
			chart = new Chart(r);
			content.add(chart);
			content.setCellHorizontalAlignment(chart,
					VerticalPanel.ALIGN_CENTER);
		}
	}

}

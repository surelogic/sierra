package com.surelogic.sierra.gwt.client.ui.panel;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.chart.Chart;
import com.surelogic.sierra.gwt.client.data.ReportSettings;

public class ChartBlock extends BlockPanel {
	private ReportSettings report;
	private Chart chart;

	public ChartBlock(final ReportSettings reportSettings) {
		super();
		setReportSettings(reportSettings);
	}

	@Override
	protected void onInitialize(final VerticalPanel content) {
		// nothing to do
	}

	public void setReportSettings(final ReportSettings r) {
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

	public ReportSettings getReportSettings() {
		return report;
	}

}

package com.surelogic.sierra.gwt.client.chart;


public class AuditContributionsChart extends ChartPanel {

	@Override
	protected Chart buildChart() {
		setTitle("Contributions");
		setSummary("In the Last 30 Days");

		return ChartBuilder.report("AuditContributions", "Contributions",
				"In the Last 30 Days").width(320).build();
	}

}

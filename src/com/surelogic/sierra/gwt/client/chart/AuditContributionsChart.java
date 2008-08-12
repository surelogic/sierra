package com.surelogic.sierra.gwt.client.chart;

import com.surelogic.sierra.gwt.client.Context;

public class AuditContributionsChart extends ChartSection {

	@Override
	protected Chart buildChart(Context context) {
		setTitle("Contributions");
		setSummary("In the Last 30 Days");

		return ChartBuilder.report("AuditContributions", "Contributions",
				"In the Last 30 Days").width(320).build();
	}

}

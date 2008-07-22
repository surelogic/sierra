package com.surelogic.sierra.gwt.client.chart;

import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ui.Chart;

public class AuditContributionsChart extends ChartSection {

	protected Chart buildChart(Context context) {
		setTitle("Contributions");
		setSummary("In the Last 30 Days");

		return ChartBuilder.name("AuditContributions").width(320).build();
	}

}

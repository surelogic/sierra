package com.surelogic.sierra.gwt.client.chart;

import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ui.Chart;

public class LatestScansChart extends ChartSection {

	protected Chart buildChart(Context context) {
		setTitle("Published Scans");
		setSummary("Latest Scan Results");

		return ChartBuilder.name("LatestScanResults").width(450).build();
	}

}

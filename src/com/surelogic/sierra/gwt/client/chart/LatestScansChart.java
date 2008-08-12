package com.surelogic.sierra.gwt.client.chart;

import com.surelogic.sierra.gwt.client.Context;

public class LatestScansChart extends ChartSection {

	@Override
	protected Chart buildChart(Context context) {
		setTitle("Published Scans");
		setSummary("Latest Scan Results");

		return ChartBuilder.report("LatestScanResults", "Published Scans",
				"Latest Scan Results").width(450).build();
	}

}

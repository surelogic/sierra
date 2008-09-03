package com.surelogic.sierra.gwt.client.chart;


public class LatestScansChart extends ChartPanel {

	@Override
	protected Chart buildChart() {
		setTitle("Published Scans");
		setSummary("Latest Scan Results");

		return ChartBuilder.report("LatestScanResults", "Published Scans",
				"Latest Scan Results").width(450).build();
	}

}

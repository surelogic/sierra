package com.surelogic.sierra.client.eclipse.jetty;

import com.surelogic.sierra.chart.IDatabasePlot;
import com.surelogic.sierra.chart.ImportanceChart;

public class ImportanceChartServlet extends SierraChartServlet {

	private final ImportanceChart f_chart = new ImportanceChart();

	@Override
	protected IDatabasePlot getChart() {
		return f_chart;
	}
}

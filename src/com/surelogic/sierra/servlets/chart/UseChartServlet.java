package com.surelogic.sierra.servlets.chart;

import com.surelogic.sierra.chart.IDatabasePlot;
import com.surelogic.sierra.chart.UseChart;

public class UseChartServlet extends SierraChartServlet {

	private static final long serialVersionUID = -6343049799320025945L;
	private final UseChart f_chart = new UseChart();

	@Override
	protected IDatabasePlot getChart() {
		return f_chart;
	}
}

package com.surelogic.sierra.chart;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.portal.PortalOverview;

public final class LatestScanResults implements IDatabasePlot {

	public JFreeChart plot(PlotSize mutableSize,
			Map<String, String[]> parameterMap, Connection c)
			throws SQLException, IOException {
		final DefaultCategoryDataset importanceData = new DefaultCategoryDataset();
		final List<ProjectOverview> overview = PortalOverview.getInstance(c)
				.getProjectOverviews();
		for (ProjectOverview po : overview) {
			importanceData.setValue((double) po.getCritical(), "Critical", po
					.getName());
			importanceData
					.setValue((double) po.getHigh(), "High", po.getName());
			importanceData.setValue((double) po.getMedium(), "Medium", po
					.getName());
			importanceData.setValue((double) po.getLow(), "Low", po.getName());
		}
		final JFreeChart chart = ChartFactory.createBarChart(
				"Latest Scan Results", null, "# of Findings", importanceData,
				PlotOrientation.HORIZONTAL, true, false, false);
		final DefaultCategoryDataset totalData = new DefaultCategoryDataset();
		for (ProjectOverview po : overview) {
			totalData.setValue((double) po.getTotalFindings(), "Total", po
					.getName());
			totalData.setValue((double) po.getCommentedFindings(), "Audited",
					po.getName());
		}
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setDataset(1, totalData);
		LineAndShapeRenderer renderer = new LineAndShapeRenderer();
		plot.setRenderer(1, renderer);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		// set the range axis to display integers only...
		chart.setBackgroundPaint(null);
		plot.getRangeAxis().setStandardTickUnits(
				NumberAxis.createIntegerTickUnits());
		return chart;
	}
}

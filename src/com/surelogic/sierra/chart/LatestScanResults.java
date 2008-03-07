package com.surelogic.sierra.chart;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.portal.PortalOverview;

public final class LatestScanResults implements IDatabasePlot {

	public JFreeChart plot(PlotSize mutableSize,
			Map<String, String[]> parameterMap, Connection c)
			throws SQLException, IOException {
		final DefaultCategoryDataset importanceData = new DefaultCategoryDataset();
		final List<ProjectOverview> overview = PortalOverview.getInstance(c)
				.getProjectOverviews();
		Collections.sort(overview, new Comparator<ProjectOverview>() {
			public int compare(ProjectOverview o1, ProjectOverview o2) {
				return o2.getTotalFindings() - o1.getTotalFindings();
			}
		});

		mutableSize.setHeight(50 * overview.size() + 100);

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

		final BarRenderer bar = (BarRenderer) chart.getCategoryPlot()
				.getRenderer();
		bar.setSeriesPaint(0, Color.RED);
		bar.setSeriesOutlinePaint(0, Color.RED);
		bar.setSeriesPaint(1, Color.BLACK);
		bar.setSeriesOutlinePaint(2, Color.BLACK);
		final Color color2 = new Color(99, 66, 0);
		bar.setSeriesPaint(2, color2);
		bar.setSeriesOutlinePaint(2, color2);
		bar.setSeriesPaint(3, Color.BLUE);
		bar.setSeriesOutlinePaint(3, Color.BLUE);

		final DefaultCategoryDataset totalData = new DefaultCategoryDataset();
		for (ProjectOverview po : overview) {
			totalData.setValue((double) po.getTotalFindings(), "Total", po
					.getName());
			totalData.setValue((double) po.getCommentedFindings(), "Audited",
					po.getName());
		}
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setDataset(1, totalData);
		LineAndShapeRenderer renderer = new LineAndShapeRenderer();
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesPaint(0, Color.BLACK);
		renderer.setSeriesShapesFilled(0, false);
		renderer.setSeriesPaint(1, Color.LIGHT_GRAY);
		renderer.setSeriesShapesFilled(1, false);
		plot.setRenderer(1, renderer);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		final TextTitle t = chart.getTitle();
		t.setPosition(RectangleEdge.TOP);
		t.setHorizontalAlignment(HorizontalAlignment.LEFT);
		t.setPaint(Color.GRAY);

		chart.setBackgroundPaint(null);
		plot.getRangeAxis().setStandardTickUnits(
				NumberAxis.createIntegerTickUnits());
		return chart;
	}
}

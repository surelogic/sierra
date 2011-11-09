package com.surelogic.sierra.chart;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.portal.PortalOverview;

/**
 * Generates a chart showing an overview of findings for the latest scans of
 * each project.
 * 
 * @author nathan
 * 
 */
public final class LatestScanResults implements IDatabasePlot {

	public JFreeChart plot(final PlotSize mutableSize,
			final ReportSettings report, final Connection c)
			throws SQLException, IOException {
		c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		final DefaultCategoryDataset importanceData = new DefaultCategoryDataset();
		final List<ProjectOverview> overview = PortalOverview.getInstance(c)
				.getProjectOverviews();
		Collections.sort(overview, new Comparator<ProjectOverview>() {
			public int compare(final ProjectOverview o1,
					final ProjectOverview o2) {
				return o2.getTotalFindings() - o1.getTotalFindings();
			}
		});

		mutableSize.setHeight(25 * overview.size() + 100);

		for (final ProjectOverview po : overview) {
			importanceData.setValue(po.getCritical(), "Critical", po.getName());
			importanceData.setValue(po.getHigh(), "High", po.getName());
			importanceData.setValue(po.getMedium(), "Medium", po.getName());
			importanceData.setValue(po.getLow(), "Low", po.getName());
		}
		final JFreeChart chart = ChartFactory.createBarChart(null, null,
				"# of Findings", importanceData, PlotOrientation.HORIZONTAL,
				true, false, false);

		final BarRenderer bar = (BarRenderer) chart.getCategoryPlot()
				.getRenderer();
		bar.setSeriesPaint(0, Color.RED);
		bar.setSeriesOutlinePaint(0, Color.RED);
		bar.setSeriesPaint(1, Color.BLACK);
		bar.setSeriesOutlinePaint(2, Color.BLACK);
		final Color brown = new Color(99, 66, 0);
		bar.setSeriesPaint(2, brown);
		bar.setSeriesOutlinePaint(2, brown);
		bar.setSeriesPaint(3, Color.GRAY);
		bar.setSeriesOutlinePaint(3, Color.GRAY);

		final DefaultCategoryDataset totalData = new DefaultCategoryDataset();
		for (final ProjectOverview po : overview) {
			totalData.setValue(po.getTotalFindings(), "Total", po.getName());
			totalData.setValue(po.getCommentedFindings(), "Audited", po
					.getName());
		}
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setDataset(1, totalData);
		final LineAndShapeRenderer renderer = new LineAndShapeRenderer();
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesPaint(0, Color.BLACK);
		renderer.setSeriesShapesFilled(0, true);
		final Color DARK_GREEN = new Color(0, 100, 0);
		renderer.setSeriesLinesVisible(1, false);
		renderer.setSeriesPaint(1, DARK_GREEN);
		renderer.setSeriesShapesFilled(1, true);
		plot.setRenderer(1, renderer);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		chart.setBackgroundPaint(null);
		plot.getRangeAxis().setStandardTickUnits(
				NumberAxis.createIntegerTickUnits());
		return chart;
	}
}

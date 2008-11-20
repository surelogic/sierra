package com.surelogic.sierra.chart;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

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

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.sierra.gwt.client.data.ReportSettings;

public class ProjectCompilationsChart implements IDatabasePlot {

	public JFreeChart plot(final PlotSize mutableSize,
			final ReportSettings report, final Connection c)
			throws SQLException, IOException {
		c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		final DefaultCategoryDataset importanceData = new DefaultCategoryDataset();
		final DefaultCategoryDataset totalData = new DefaultCategoryDataset();
		final String projectName = report.getSettingValue("projectName", 0);
		final Query q = new ConnectionQuery(c);
		final boolean bykLoC = "true".equals(report.getSettingValue("kLoC", 0));
		final Map<String, Integer> kLoCMap = bykLoC ? q.prepared(
				"Plots.Project.linesOfCodeByPackage",
				new ResultHandler<Map<String, Integer>>() {
					public Map<String, Integer> handle(final Result result) {
						final Map<String, Integer> map = new HashMap<String, Integer>();
						for (final Row r : result) {
							map.put(r.nextString(), r.nextInt());
						}
						return map;
					}
				}).call(projectName)
				: null;
		if (projectName != null) {
			final Map<String, Integer> totals = new TreeMap<String, Integer>();
			q.prepared("Plots.Project.compilations", new RowHandler<Void>() {
				public Void handle(final Row r) {
					final int count = r.nextInt();
					final String importance = r.nextString();
					final String pakkage = r.nextString();
					if (bykLoC) {
						importanceData.setValue((double) count
								/ kLoCMap.get(pakkage) * 1000, importance,
								pakkage);
					} else {
						importanceData.setValue(count, importance, pakkage);
					}
					final Integer total = totals.get(pakkage);
					totals.put(pakkage, (total == null ? 0 : total) + count);
					return null;
				}
			}).call(projectName);
			for (final Entry<String, Integer> entry : totals.entrySet()) {
				final String pakkage = entry.getKey();
				final double count = entry.getValue();
				if (bykLoC) {
					totalData.setValue(count / kLoCMap.get(pakkage) * 1000,
							"Total", entry.getKey());
				} else {
					totalData.setValue(entry.getValue(), "Total", entry
							.getKey());
				}
			}
		}
		mutableSize.setHeight(25 * importanceData.getColumnCount() + 100);

		final JFreeChart chart = ChartFactory.createBarChart(
				"Latest Scan Findings By Package", null, "# of Findings"
						+ (bykLoC ? " / kLoC" : ""), importanceData,
				PlotOrientation.HORIZONTAL, true, false, false);
		final BarRenderer bar = (BarRenderer) chart.getCategoryPlot()
				.getRenderer();

		bar.setSeriesPaint(0, Color.GRAY);
		bar.setSeriesOutlinePaint(0, Color.GRAY);
		final Color brown = new Color(99, 66, 0);
		bar.setSeriesPaint(1, brown);
		bar.setSeriesOutlinePaint(1, brown);
		bar.setSeriesPaint(2, Color.BLACK);
		bar.setSeriesOutlinePaint(2, Color.BLACK);
		bar.setSeriesPaint(3, Color.RED);
		bar.setSeriesOutlinePaint(3, Color.RED);

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setDataset(1, totalData);
		final LineAndShapeRenderer renderer = new LineAndShapeRenderer();
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesPaint(0, Color.BLACK);
		renderer.setSeriesShapesFilled(0, true);
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

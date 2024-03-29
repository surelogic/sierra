package com.surelogic.sierra.chart;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.tool.message.Importance;

public class FindingsByProject implements IDatabasePlot {

	public JFreeChart plot(final PlotSize mutableSize,
			final ReportSettings report, final Connection c)
			throws SQLException, IOException {
		final List<String> projects = report.getSettingValue("Projects");
		final List<String> importancesNames = report.getSettingValue("Importance");
		final DefaultCategoryDataset importanceData = new DefaultCategoryDataset();
		final DefaultCategoryDataset totalData = new DefaultCategoryDataset();
		if (projects != null) {
			if (!projects.isEmpty()) {
				final StringBuilder projectStr = new StringBuilder();
				for (final String p : projects) {
					projectStr.append("'");
					projectStr.append(JDBCUtils.escapeString(p));
					projectStr.append("',");
				}
				projectStr.setLength(projectStr.length() - 1);
				// Extract the importances we want, or return all of them if
				// they are not specified
				List<Importance> importances;
				if (importancesNames == null || importancesNames.isEmpty()) {
					importances = Importance.standardValues();
				} else {
					importances = new ArrayList<Importance>();
					for (final String imp : importancesNames) {
						importances.add(Importance.fromValue(imp));
					}
				}
				final StringBuilder impStr = new StringBuilder();
				for (final Importance i : importances) {
					impStr.append(i.ordinal());
					impStr.append(",");
				}
				impStr.setLength(impStr.length() - 1);
				final Map<String, Integer> totals = new TreeMap<String, Integer>();
				new ConnectionQuery(c).statement(
						"Plots.Project.scanFindingsByProjectAndImportance",
						new RowHandler<Void>() {
							public Void handle(final Row r) {
								final int count = r.nextInt();
								final String importance = r.nextString();
								final String time = r.nextString();
								importanceData
										.setValue(count, importance, time);
								final Integer total = totals.get(time);
								totals.put(time, (total == null ? 0 : total)
										+ count);
								return null;
							}
						}).call(projectStr.toString(), impStr.toString());
				for (final Entry<String, Integer> entry : totals.entrySet()) {
					totalData.setValue(entry.getValue(), "Total", entry
							.getKey());
				}
			}
		}
		mutableSize.setHeight(25 * importanceData.getColumnCount() + 100);
		final JFreeChart chart = ChartFactory.createBarChart(
				"Findings Breakdown Per Scan", null, "# of Findings",
				importanceData, PlotOrientation.HORIZONTAL, true, false, false);

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

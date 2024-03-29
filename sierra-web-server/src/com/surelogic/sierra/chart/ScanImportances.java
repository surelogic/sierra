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

public class ScanImportances implements IDatabasePlot {

	public JFreeChart plot(final PlotSize mutableSize,
			final ReportSettings report, final Connection c)
			throws SQLException, IOException {
		final String scan = report.getSettingValue("scan", 0);
		final List<String> importancesNames = report
				.getSettingValue("importance");
		final List<String> packages = report.getSettingValue("package");
		final DefaultCategoryDataset importanceData = new DefaultCategoryDataset();
		final DefaultCategoryDataset totalData = new DefaultCategoryDataset();
		if (scan != null && !(scan.length() == 0)) {
			final List<Importance> importances;
			if ((importancesNames == null) || importancesNames.isEmpty()) {
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
			if ((packages != null) && !packages.isEmpty()) {
				final StringBuilder packageStr = new StringBuilder();
				for (final String pakkage : packages) {
					packageStr.append("'");
					packageStr.append(JDBCUtils.escapeString(pakkage));
					packageStr.append("'");
					packageStr.append(",");
				}
				packageStr.setLength(packageStr.length() - 1);
				final Map<String, Integer> totals = new TreeMap<String, Integer>();
				new ConnectionQuery(c).statement(
						"Plots.Scan.packageImportances",
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
						}).call(JDBCUtils.escapeString(scan),
						packageStr.toString(), impStr.toString());
				for (final Entry<String, Integer> entry : totals.entrySet()) {
					totalData.setValue(entry.getValue(), "Total", entry
							.getKey());
				}
			}
		}
		mutableSize.setHeight(25 * importanceData.getColumnCount() + 100);
		final JFreeChart chart = ChartFactory.createBarChart(
				"Finding Importances Per Package", null, "# of Findings",
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
		plot.getDomainAxis().setLowerMargin(0.0);
		plot.getDomainAxis().setUpperMargin(0.0);
		return chart;
	}
}

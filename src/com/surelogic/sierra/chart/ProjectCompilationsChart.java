package com.surelogic.sierra.chart;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.sierra.gwt.client.data.Report;

public class ProjectCompilationsChart implements IDatabasePlot {

	public JFreeChart plot(PlotSize mutableSize, Report report, Connection c)
			throws SQLException, IOException {
		c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		final DefaultCategoryDataset importanceData = new DefaultCategoryDataset();
		final String projectName = report.getParameter("projectName")
				.getValue();
		if (projectName != null) {
			new ConnectionQuery(c).prepared("Plots.Project.compilations",
					new RowHandler<Void>() {
						public Void handle(Row r) {
							importanceData.setValue(r.nextInt(),
									r.nextString(), r.nextString());
							return null;
						}
					}).call(projectName);
		}
		final JFreeChart chart = ChartFactory.createBarChart("Project Results",
				null, "# of Findings", importanceData,
				PlotOrientation.HORIZONTAL, true, false, false);

		final BarRenderer bar = (BarRenderer) chart.getCategoryPlot()
				.getRenderer();

		bar.setSeriesPaint(1, Color.GRAY);
		bar.setSeriesOutlinePaint(1, Color.GRAY);
		bar.setSeriesPaint(2, Color.BLACK);
		bar.setSeriesOutlinePaint(2, Color.BLACK);
		final Color brown = new Color(99, 66, 0);
		bar.setSeriesPaint(2, brown);
		bar.setSeriesOutlinePaint(2, brown);
		bar.setSeriesPaint(3, Color.RED);
		bar.setSeriesOutlinePaint(3, Color.RED);
		final CategoryPlot plot = chart.getCategoryPlot();
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

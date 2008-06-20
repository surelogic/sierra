package com.surelogic.sierra.chart;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryGraph;

public class CategoryCounts implements IDatabasePlot {

	public JFreeChart plot(PlotSize mutableSize, Report report, Connection c)
			throws SQLException, IOException {
		c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		final String uid = report.getParameter("uuid").getValue();
		if (uid != null) {
			final ConnectionQuery q = new ConnectionQuery(c);
			final CategoryGraph g = new Categories(q).getCategoryGraph(uid);
			if (g != null) {
				final StringBuffer uidList = new StringBuffer();
				for (final String ft : g.getFindingTypes()) {
					uidList.append("'");
					uidList.append(ft);
					uidList.append("'");
					uidList.append(",");
				}
				final DefaultCategoryDataset set = new DefaultCategoryDataset();
				// Only fill in the dataset if we actually have finding types in
				// the category
				if (uidList.length() > 0) {
					uidList.setLength(uidList.length() - 1);
					q.statement("Plots.FindingTypes.count",
							new ResultHandler<Void>() {

								public Void handle(Result r) {
									for (final Row row : r) {
										set.setValue(row.nextInt(), "Findings",
												row.nextString());
									}
									return null;
								}
							}).call(uidList.toString());
				}
				final JFreeChart chart = ChartFactory.createBarChart(
						"Frequency", null, "# in the latest scan", set,
						PlotOrientation.HORIZONTAL, true, false, false);
				chart.setBackgroundPaint(null);
				return chart;
			}
		}
		return null;
	}

}

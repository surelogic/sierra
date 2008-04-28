package com.surelogic.sierra.chart;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;

public class FindingTypeCounts implements IDatabasePlot {

	public JFreeChart plot(PlotSize mutableSize,
			Map<String, String> parameters, Connection c) throws SQLException,
			IOException {
		c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		final String uid = parameters.get("uid");
		if (uid != null) {
			final DefaultCategoryDataset set = new ConnectionQuery(c).prepared(
					"Plots.FindingType.count",
					new ResultHandler<DefaultCategoryDataset>() {

						public DefaultCategoryDataset handle(Result r) {
							DefaultCategoryDataset set = new DefaultCategoryDataset();
							for (Row row : r) {
								set.setValue(row.nextInt(), "Findings", row
										.nextString());
							}
							return set;
						}
					}).call(uid);
			final JFreeChart chart = ChartFactory.createBarChart("Frequency",
					null, "# in the latest scan", set,
					PlotOrientation.HORIZONTAL, true, false, false);
			return chart;
		}

		return null;
	}

}

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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.portal.PortalOverview;

public final class UseChart implements IDatabasePlot {

	public JFreeChart plot(Map<String, String[]> parameterMap, Connection c)
			throws SQLException, IOException {

		final PortalOverview po = PortalOverview.getInstance(c);
		List<UserOverview> userOverviewList = po.getUserOverviews();

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (UserOverview uo : userOverviewList) {
			dataset.setValue((double) (uo.getAudits() + 10), "Audits", uo
					.getUserName());
		}
		dataset.setValue(20.0, "Audits", "Hard Worker");
		dataset.setValue(1.0, "Audits", "Slacker");
		final JFreeChart chart = ChartFactory.createBarChart("Audits by User",
				"User", "Audits", dataset, PlotOrientation.VERTICAL, false,
				true, false);
		chart.setBackgroundPaint(null);
		final CategoryPlot plot = chart.getCategoryPlot();

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		return chart;
	}
}

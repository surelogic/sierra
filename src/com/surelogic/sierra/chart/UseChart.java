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

import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.portal.PortalOverview;

public final class UseChart implements IDatabasePlot {

	public JFreeChart plot(Map<String, String[]> parameterMap, Connection c)
			throws SQLException, IOException {
		final String name = parameterMap.get("name")[0];
		if ("users".equals(name)) {
			return userOverview(parameterMap, c);
		} else if ("projects".equals(name)) {
			return projectsOverview(parameterMap, c);
		}
		return null;
	}

	public JFreeChart projectsOverview(Map<String, String[]> parameterMap,
			Connection c) throws SQLException, IOException {
		final DefaultCategoryDataset data = new DefaultCategoryDataset();

		for (ProjectOverview po : PortalOverview.getInstance(c)
				.getProjectOverviews()) {
			data.setValue((double) po.getCritical(), "Critical", po.getName());
			data.setValue((double) po.getHigh(), "High", po.getName());
			data.setValue((double) po.getMedium(), "Medium", po.getName());
			data.setValue((double) po.getLow(), "Low", po.getName());
		}
		final JFreeChart chart = ChartFactory.createBarChart(
				"Latest Scan Results", null, "Importance", data,
				PlotOrientation.HORIZONTAL, true, false, false);
		/*
		 * final JFreeChart chart = ChartFactory.createStackedBarChart(
		 * "Importance by Project", "Project", "Importance", data,
		 * PlotOrientation.VERTICAL, true, true, false);
		 */
		// set the range axis to display integers only...
		chart.setBackgroundPaint(null);
		chart.getCategoryPlot().getRangeAxis().setStandardTickUnits(
				NumberAxis.createIntegerTickUnits());
		return chart;
	}

	public JFreeChart userOverview(Map<String, String[]> parameterMap,
			Connection c) throws SQLException, IOException {

		final PortalOverview po = PortalOverview.getInstance(c);
		List<UserOverview> userOverviewList = po.getUserOverviews();

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (UserOverview uo : userOverviewList) {
			dataset.setValue((double) (uo.getAudits()), "Audits", uo
					.getUserName());
		}
		final JFreeChart chart = ChartFactory.createBarChart("Contributions",
				"User", "Audits", dataset, PlotOrientation.HORIZONTAL, false,
				false, false);
		chart.setBackgroundPaint(null);
		final CategoryPlot plot = chart.getCategoryPlot();

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		return chart;
	}
}

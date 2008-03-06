package com.surelogic.sierra.chart;

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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.portal.PortalOverview;

public final class AuditContributions implements IDatabasePlot {

	public JFreeChart plot(PlotSize mutableSize,
			Map<String, String[]> parameterMap, Connection c)
			throws SQLException, IOException {
		final PortalOverview po = PortalOverview.getInstance(c);
		List<UserOverview> userOverviewList = po.getUserOverviews();
		Collections.sort(userOverviewList, new Comparator<UserOverview>() {
			public int compare(UserOverview o1, UserOverview o2) {
				return o2.getAudits() - o1.getAudits();
			}
		});
		mutableSize.setHeight(20 * userOverviewList.size() + 50);
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (UserOverview uo : userOverviewList) {
			dataset.setValue((double) (uo.getAudits()), "Audits", uo
					.getUserName());
		}

		final JFreeChart chart = ChartFactory.createBarChart(
				"Audit Contributions", "User", "Audits", dataset,
				PlotOrientation.HORIZONTAL, false, false, false);
		chart.setBackgroundPaint(null);
		final CategoryPlot plot = chart.getCategoryPlot();

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		return chart;
	}
}

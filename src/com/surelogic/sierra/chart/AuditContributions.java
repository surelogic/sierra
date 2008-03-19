package com.surelogic.sierra.chart;

import java.awt.Color;
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
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.portal.PortalOverview;

public final class AuditContributions implements IDatabasePlot {

	public JFreeChart plot(PlotSize mutableSize,
			Map<String, String> parameters, Connection c)
			throws SQLException, IOException {
		c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		final PortalOverview po = PortalOverview.getInstance(c);
		List<UserOverview> userOverviewList = po.getEnabledUserOverviews();
		Collections.sort(userOverviewList, new Comparator<UserOverview>() {
			public int compare(UserOverview o1, UserOverview o2) {
				return o2.getAudits() - o1.getAudits();
			}
		});

		mutableSize.setHeight(35 * userOverviewList.size() + 100);

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (UserOverview uo : userOverviewList) {
			dataset.setValue((double) (uo.getAudits()), "Audits", uo
					.getUserName());
			dataset.setValue((double) (uo.getFindings()), "Findings Examined",
					uo.getUserName());
		}
		final JFreeChart chart = ChartFactory.createBarChart("Contributions",
				null, "# in the last 30 days", dataset,
				PlotOrientation.HORIZONTAL, true, false, false);

		final CategoryPlot plot = chart.getCategoryPlot();
		final BarRenderer bar = (BarRenderer) plot.getRenderer();
		bar.setSeriesPaint(0, Color.BLACK);
		bar.setSeriesOutlinePaint(0, Color.BLACK);
		bar.setSeriesPaint(1, Color.GRAY);
		bar.setSeriesOutlinePaint(1, Color.GRAY);

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

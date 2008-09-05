package com.surelogic.sierra.chart;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.portal.PortalOverview;

/**
 * Generates a chart of the number of audits made in the past 30 days per user.
 * 
 * @author nathan
 * 
 */
public final class AuditContributions implements IDatabasePlot {

	public JFreeChart plot(final PlotSize mutableSize,
			final ReportSettings report, final Connection c)
			throws SQLException, IOException {
		c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		final PortalOverview po = PortalOverview.getInstance(c);
		final List<UserOverview> userOverviewList = po
				.getEnabledUserOverviews();
		Collections.sort(userOverviewList, new Comparator<UserOverview>() {
			public int compare(final UserOverview o1, final UserOverview o2) {
				return o2.getAudits() - o1.getAudits();
			}
		});

		mutableSize.setHeight(35 * userOverviewList.size() + 100);

		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (final UserOverview uo : userOverviewList) {
			dataset.setValue(uo.getAudits(), "Audits", uo.getUserName());
			dataset.setValue(uo.getFindings(), "Findings Examined", uo
					.getUserName());
		}
		final JFreeChart chart = ChartFactory.createBarChart(null, null,
				"# of Comments", dataset, PlotOrientation.HORIZONTAL, true,
				false, false);

		final CategoryPlot plot = chart.getCategoryPlot();
		final BarRenderer bar = (BarRenderer) plot.getRenderer();
		bar.setSeriesPaint(0, Color.BLACK);
		bar.setSeriesOutlinePaint(0, Color.BLACK);
		bar.setSeriesPaint(1, Color.GRAY);
		bar.setSeriesOutlinePaint(1, Color.GRAY);

		chart.setBackgroundPaint(null);
		plot.getRangeAxis().setStandardTickUnits(
				NumberAxis.createIntegerTickUnits());

		return chart;
	}
}

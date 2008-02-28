package com.surelogic.sierra.chart;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.surelogic.common.logging.SLLogger;

public final class UseChart implements IDatabasePlot {

	public JFreeChart plot(Map<String, String[]> parameterMap, Connection c)
			throws SQLException, IOException {

		/*
		 * Filter this list based upon the parameterMap projects
		 */
		String[] projectNames = parameterMap.get("project");
		final String projectName;
		if (projectNames != null && projectNames.length > 0) {
			projectName = projectNames[0];
		} else {
			projectName = "unknown";
		}
		SLLogger.getLogger().info("Plot of importance for " + projectName);

		/*
		 * Query counts for each project
		 */
		final Map<String, Integer> counts = new HashMap<String, Integer>();
		final Statement st = c.createStatement();
		try {
			StringBuilder b = new StringBuilder();
			b.append("select Importance, count(*) from FINDINGS_OVERVIEW");
			b.append(" where Status != 'Fixed' and Project = '");
			b.append(projectName);
			b.append("' group by Importance");

			final String query = b.toString();
			if (SLLogger.getLogger().isLoggable(Level.FINE)) {
				SLLogger.getLogger().fine(
						"Importance plot counts query: " + query);
			}
			final ResultSet rs = st.executeQuery(query);
      try {
        while (rs.next()) {
          final String value = rs.getString(1);
          int count = rs.getInt(2);
          counts.put(value, count);
        }
      } finally {
        rs.close();
      }
		} finally {
			st.close();
		}
		Integer cw;
		int criticalCount = 0;
		cw = counts.get("Critical");
		if (cw != null)
			criticalCount = cw;
		int highCount = 0;
		cw = counts.get("High");
		if (cw != null)
			highCount = cw;
		int mediumCount = 0;
		cw = counts.get("Medium");
		if (cw != null)
			mediumCount = cw;
		int lowCount = 0;
		cw = counts.get("Low");
		if (cw != null)
			lowCount = cw;

		/*
		 * Plot the counts for each project
		 */

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.setValue(criticalCount, projectName, "Critical");
		dataset.setValue(highCount, projectName, "High");
		dataset.setValue(mediumCount, projectName, "Medium");
		dataset.setValue(lowCount, projectName, "Low");
		final JFreeChart chart = ChartFactory.createBarChart(
				"Findings by Importance for " + projectName, "Importance",
				"Findings", dataset, PlotOrientation.HORIZONTAL, false, true,
				false);
		chart.setBackgroundPaint(null);
		return chart;
	}
}

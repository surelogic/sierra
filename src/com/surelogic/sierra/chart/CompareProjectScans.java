package com.surelogic.sierra.chart;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.jfree.chart.JFreeChart;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;

public class CompareProjectScans implements IDatabasePlot {

	public JFreeChart plot(final PlotSize mutableSize, final Report report,
			final Connection c) throws SQLException, IOException {
		final Parameter first = report.getParameter("first");
		final Parameter second = report.getParameter("second");
		if ((first != null) && (second != null)) {
			final Query q = new ConnectionQuery(c);
			final String firstStr = first.getValue();
			final String secondStr = second.getValue();
			if ((firstStr != null) && !firstStr.isEmpty()
					&& (secondStr != null) && !secondStr.isEmpty()) {
				q.prepared("Plots.Project.compareScans").call(firstStr,
						secondStr);
			}
		}
		return null;
	}

}

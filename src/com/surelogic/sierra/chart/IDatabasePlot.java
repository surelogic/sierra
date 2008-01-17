package com.surelogic.sierra.chart;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.jfree.chart.JFreeChart;

/**
 * The interface for plotting all Sierra charts and graphs.
 */
public interface IDatabasePlot {

	/**
	 * Constructs a chart based upon the passed parameters and the current data
	 * in the database.
	 * 
	 * @param parameterMap
	 *            the non-null, but possibly empty, map of the parameters to be
	 *            considered when creating the chart.
	 * @param c
	 *            a non-null connection to the Sierra database.
	 * @return the resulting chart.
	 */
	JFreeChart plot(final Map<String, String[]> parameterMap, final Connection c)
			throws SQLException, IOException;
}

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
	 *            a non-null connection to the Sierra database. This connection
	 *            will be closed by the caller of this method.
	 * @return the resulting chart.
	 */
	JFreeChart plot(final Map<String, String[]> parameterMap, final Connection c)
			throws SQLException, IOException;

	/**
	 * Gets the desired width of this plot in pixels. Only plots that change
	 * their size based upon the data that they display need to do anything but
	 * return the hint parameter.
	 * 
	 * @param hint
	 *            a hint as to what the desired width in pixels.
	 * @return the desired width of this plot in pixels.
	 */
	int getWidth(final int hint);

	/**
	 * Gets the desired height of this plot in pixels. Only plots that change
	 * their size based upon the data that they display need to do anything but
	 * return the hint parameter.
	 * 
	 * @param hint
	 *            a hint as to what the desired height in pixels.
	 * @return the desired height of this plot in pixels.
	 */
	int getHeight(final int hint);
}

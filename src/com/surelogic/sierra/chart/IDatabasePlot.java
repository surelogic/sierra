package com.surelogic.sierra.chart;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.jfree.chart.JFreeChart;

/**
 * The interface for plotting all Sierra charts and graphs. Implementing objects
 * should be safe to shared by several servlet instances. Thus, these objects
 * should not contain any state.
 */
public interface IDatabasePlot {

	/**
	 * Constructs a chart based upon the passed parameters and the current data
	 * in the database.
	 * 
	 * @param mutableSize
	 *            of the plot. Typically this parameter may be ignored, however,
	 *            if a plot changes its size based upon the data it is
	 *            displaying then the width and height values passed into this
	 *            call should be examined and changed as desired.
	 * @param parameterMap
	 *            the non-null, but possibly empty, map of the parameters to be
	 *            considered when creating the chart.
	 * @param c
	 *            a non-null connection to the Sierra database. This connection
	 *            will be closed by the caller of this method.
	 * @return the resulting chart.
	 */
	JFreeChart plot(PlotSize mutableSize, Map<String, String[]> parameterMap,
			Connection c) throws SQLException, IOException;
}

package com.surelogic.sierra.chart;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.jfree.chart.JFreeChart;

import com.surelogic.sierra.gwt.client.data.ReportSettings;

/**
 * The interface for plotting all Sierra charts and graphs.
 * <p>
 * Implementing objects should be safe to shared by several servlet instances.
 * Thus, these objects should not contain any state.
 * <p>
 * Implementing objects must have a no-argument constructor.
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
	 * @param parameters
	 *            the report to be considered when producing the chart.
	 * @param c
	 *            a non-null connection to the Sierra database. This connection
	 *            will be closed by the caller of this method.
	 * @return the resulting chart.
	 */
	JFreeChart plot(PlotSize mutableSize, ReportSettings report, Connection c)
			throws SQLException, IOException;
}

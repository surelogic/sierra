package com.surelogic.sierra.client.eclipse.jetty;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.surelogic.sierra.chart.IDatabasePlot;
import com.surelogic.sierra.client.eclipse.Data;

/**
 * Abstract base class for all Sierra charts. Subclasses typically only need to
 * implement {@link #getChart()}.
 * <p>
 * The width and height parameters to a <i>GET</i> request are used to set the
 * width and height of the resulting chart image.
 * <p>
 * The resulting chart image is always a in <i>PNG</i> format (content type is
 * set to <tt>"image/png"</tt>).
 */
public abstract class SierraChartServlet extends HttpServlet {

	/**
	 * Returns the chart generation object used to plot the chart returned by
	 * this servlet.
	 * 
	 * @return a chart generation object.
	 */
	protected abstract IDatabasePlot getChart();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Map<String, String[]> parameterMap = launderRequestParameters(req);

		resp.setContentType("image/png");

		try {
			final Connection c = Data.getConnection();
			try {
				final JFreeChart chart = getChart().plot(parameterMap, c);
				ChartUtilities.writeChartAsPNG(resp.getOutputStream(), chart,
						getWidth(parameterMap), getHeight(parameterMap), true,
						9);
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			/*
			 * Wrap the SQL exception into an IOException and let the container
			 * report it.
			 */
			throw new IOException("Using the Sierra db to plot a chart.", e);
		}
	}

	public int getWidth(Map<String, String[]> parameterMap) {
		String[] values;
		values = parameterMap.get("width");
		if (values != null && values.length == 1) {
			try {
				final int width = Integer.parseInt(values[0]);
				return width;
			} catch (NumberFormatException ignore) {
				// ignore, just use the default width
			}
		}
		return 400; // default width
	}

	public int getHeight(Map<String, String[]> parameterMap) {
		String[] values;
		values = parameterMap.get("height");
		if (values != null && values.length == 1) {
			try {
				final int height = Integer.parseInt(values[0]);
				return height;
			} catch (NumberFormatException ignore) {
				// ignore, just use the default height
			}
		}
		return 400; // default height
	}

	@SuppressWarnings("unchecked")
	private Map<String, String[]> launderRequestParameters(
			HttpServletRequest req) {
		final Map<String, String[]> parameterMap = req.getParameterMap();
		return parameterMap;
	}
}

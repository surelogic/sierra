package com.surelogic.sierra.servlets.chart;

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
import com.surelogic.sierra.chart.PlotSize;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;

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
	protected void doGet(HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final Map<String, String[]> parameterMap = launderRequestParameters(req);

		resp.setContentType("image/png");

		ConnectionFactory.withReadOnly(new ServerTransaction<Void>() {
			public Void perform(Connection conn, Server server)
					throws SQLException {
				try {
					final PlotSize mutableSize = new PlotSize(
							getWidthHint(parameterMap),
							getHeightHint(parameterMap));
					final JFreeChart chart = getChart().plot(mutableSize,
							parameterMap, conn);
					ChartUtilities.writeChartAsPNG(resp.getOutputStream(),
							chart, mutableSize.getWidth(), mutableSize
									.getHeight(), true, 9);
					return null;
				} catch (IOException e) {
					SQLException sqle = new SQLException();
					sqle.initCause(e);
					throw sqle;
				}
			}
		});
	}

	/**
	 * Extracts the {@code width} parameter from the servlet parameters and
	 * returns its value.
	 * 
	 * @param parameterMap
	 *            the servlet parameters.
	 * @return the value of the {@code width} parameter or 400 it it is not set.
	 */
	private int getWidthHint(Map<String, String[]> parameterMap) {
		int widthHint = 400;
		final String[] values = parameterMap.get("width");
		if (values != null && values.length == 1) {
			try {
				final int width = Integer.parseInt(values[0]);
				widthHint = width;
			} catch (NumberFormatException ignore) {
				// ignore, just use the default width
			}
		}
		return widthHint;
	}

	/**
	 * Extracts the {@code height} parameter from the servlet parameters and
	 * returns its value.
	 * 
	 * @param parameterMap
	 *            the servlet parameters.
	 * @return the value of the {@code height} parameter or 400 it it is not
	 *         set.
	 */
	private int getHeightHint(Map<String, String[]> parameterMap) {
		int heightHint = 400;
		final String[] values = parameterMap.get("height");
		if (values != null && values.length == 1) {
			try {
				final int height = Integer.parseInt(values[0]);
				heightHint = height;
			} catch (NumberFormatException ignore) {
				// ignore, just use the default height
			}
		}
		return heightHint;
	}

	@SuppressWarnings("unchecked")
	private Map<String, String[]> launderRequestParameters(
			HttpServletRequest req) {
		final Map<String, String[]> parameterMap = req.getParameterMap();
		return parameterMap;
	}
}

package com.surelogic.sierra.servlets.chart;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.chart.IDatabasePlot;
import com.surelogic.sierra.chart.PlotSize;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;

/**
 * Servlet class for all Sierra charts. The {@code init-param}
 * {@code plot-class} must be set to the fully qualified name of a class that is
 * a subtype of {@link IDatabasePlot}. For example, the <tt>web.xml</tt> for
 * the last scan results chart is:
 * 
 * <pre>
 * &lt;servlet&gt;
 *  &lt;servlet-name&gt;LatestScanResults&lt;/servlet-name&gt;
 *  &lt;servlet-class&gt;
 *   com.surelogic.sierra.servlets.chart.SierraChartServlet
 *  &lt;/servlet-class&gt;
 *  &lt;init-param&gt;
 *   &lt;param-name&gt;plot-class&lt;/param-name&gt;
 *   &lt;param-value&gt;
 *    com.surelogic.sierra.chart.LatestScanResults
 *   &lt;/param-value&gt;
 *  &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * 
 * &lt;servlet-mapping&gt;
 *  &lt;servlet-name&gt;LatestScanResults&lt;/servlet-name&gt;
 *  &lt;url-pattern&gt;/chart/LatestScanResults&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 * 
 * The width and height parameters to a <i>GET</i> request are used to set the
 * width and height of the resulting chart image.
 * <p>
 * The resulting chart image is always a in <i>PNG</i> format (content type is
 * set to <tt>"image/png"</tt>).
 */
public class SierraChartServlet extends HttpServlet {

	/**
	 * The chart generation object this server uses to create a chart based upon
	 * the data in the Sierra database.
	 */
	private final AtomicReference<IDatabasePlot> f_plot = new AtomicReference<IDatabasePlot>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		final String plotClassName = config.getInitParameter("plot-class");
		if (plotClassName == null) {
			throw new ServletException(I18N.err(81, getServletName()));
		}
		try {
			final IDatabasePlot plot = (IDatabasePlot) Class.forName(
					plotClassName).newInstance();
			f_plot.set(plot);
		} catch (ClassCastException e) {
			throw new ServletException(I18N.err(82, plotClassName,
					getServletName()), e);
		} catch (InstantiationException e) {
			throw new ServletException(I18N.err(82, plotClassName,
					getServletName()), e);
		} catch (IllegalAccessException e) {
			throw new ServletException(I18N.err(82, plotClassName,
					getServletName()), e);
		} catch (ClassNotFoundException e) {
			throw new ServletException(I18N.err(82, plotClassName,
					getServletName()), e);
		}
	}

	/**
	 * Returns the chart generation object used to plot the chart returned by
	 * this servlet.
	 * 
	 * @return a chart generation object.
	 */
	protected IDatabasePlot getChart() {
		final IDatabasePlot result = f_plot.get();
		if (result == null) {
			/*
			 * This is probably due to init not being called.
			 */
			throw new IllegalStateException(I18N.err(44, "f_plot"));
		} else {
			return result;
		}
	}

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
	 * @return the value of the {@code width} parameter or 400 if it is not set.
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
	 * @return the value of the {@code height} parameter or 400 if it is not
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

	/**
	 * Changes the type of the servlet parameters.
	 * 
	 * @param req
	 *            the servlet parameters.
	 * @return the servlet parameters as a map.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String[]> launderRequestParameters(
			HttpServletRequest req) {
		final Map<String, String[]> parameterMap = req.getParameterMap();
		return parameterMap;
	}

	private static final long serialVersionUID = 5040888860303280445L;
}

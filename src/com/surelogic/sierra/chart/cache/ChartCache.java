package com.surelogic.sierra.chart.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.chart.IDatabasePlot;
import com.surelogic.sierra.chart.PlotSize;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.servlets.ServletUtility;

/**
 * This class is thread safe.
 */
public final class ChartCache {

	public void sendPNG(final Ticket ticket, final HttpServletResponse response)
			throws ServletException, IOException {
		sendCacheFile(ticket, getPNGFileFor(ticket), "image/png", response);
	}

	public void sendMAP(final Ticket ticket, final HttpServletResponse response)
			throws ServletException, IOException {
		sendCacheFile(ticket, getMAPFileFor(ticket), "text/plain", response);
	}

	private static final String CHART_CACHE_FILE_PREFIX = "chart-";

	private File getPNGFileFor(final Ticket ticket) {
		return new File(FileUtility.getSierraTeamServerCacheDirectory()
				+ File.separator + CHART_CACHE_FILE_PREFIX
				+ ticket.getUUID().toString() + ".png");
	}

	private File getMAPFileFor(final Ticket ticket) {
		return new File(FileUtility.getSierraTeamServerCacheDirectory()
				+ File.separator + CHART_CACHE_FILE_PREFIX
				+ ticket.getUUID().toString() + ".map");
	}

	private void sendCacheFile(final Ticket ticket, final File file,
			final String mimeType, final HttpServletResponse response)
			throws ServletException, IOException {
		boolean createOrUpdateCacheFiles = true;
		/*
		 * Does a cached file exist?
		 */
		if (file.exists()) {
			/*
			 * Is it OK to use cached file?
			 */
			final Date modified = new Date(file.lastModified());
			// TODO add check against the last database change.
			createOrUpdateCacheFiles = false;
		}
		if (createOrUpdateCacheFiles) {
			createCacheFiles(ticket);
		}
		ServletUtility.sendCacheFile(file, response, mimeType);
	}

	private void createCacheFiles(final Ticket ticket) throws ServletException {
		final String type = ticket.getParameters().get("type");
		if (type == null) {
			throw new ServletException(I18N.err(96, ticket.toString()));
		}
		final IDatabasePlot plotter = getPlotter(type);

		ConnectionFactory.withReadOnly(new ServerTransaction<Void>() {
			public Void perform(Connection conn, Server server)
					throws SQLException {
				try {
					final PlotSize mutableSize = new PlotSize(
							getWidthHint(ticket.getParameters()),
							getHeightHint(ticket.getParameters()));
					final JFreeChart chart = plotter.plot(mutableSize, ticket
							.getParameters(), conn);

					setupTooltips(chart);
					final ChartRenderingInfo info = new ChartRenderingInfo(
							new StandardEntityCollection());
					/*
					 * Output PNG image file.
					 */
					final File pngFile = getPNGFileFor(ticket);
					final OutputStream pngWriter = new FileOutputStream(pngFile);
					ChartUtilities.writeChartAsPNG(pngWriter, chart,
							mutableSize.getWidth(), mutableSize.getHeight(),
							info, true, 9);

					/*
					 * Output image map file.
					 */
					final File mapFile = getMAPFileFor(ticket);
					final PrintWriter mapWriter = new PrintWriter(mapFile);
					ChartUtilities.writeImageMap(mapWriter, "map", info, false);
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
	 * All access to this map is protected by a lock on the object.
	 */
	private final Map<String, IDatabasePlot> f_typeToPlotter = new HashMap<String, IDatabasePlot>();

	private IDatabasePlot getPlotter(String type) throws ServletException {
		assert type != null;

		IDatabasePlot plotter;
		synchronized (f_typeToPlotter) {
			plotter = f_typeToPlotter.get(type);
			if (plotter == null) {
				/*
				 * Try to find the plotter, either it doesn't exist (in which
				 * case we throw an exception) or this is the first time it has
				 * been requested.
				 */
				final String plotClassName = "com.surelogic.sierra.chart."
						+ type;
				try {
					plotter = (IDatabasePlot) Class.forName(plotClassName)
							.newInstance();
					f_typeToPlotter.put(type, plotter);
				} catch (ClassCastException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				} catch (InstantiationException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				} catch (IllegalAccessException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				} catch (ClassNotFoundException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				}
			}
		}
		return plotter;
	}

	/**
	 * Extracts the {@code width} parameter from the servlet parameters and
	 * returns its value.
	 * 
	 * @param parameters
	 *            the servlet parameters.
	 * @return the value of the {@code width} parameter or 400 if it is not set.
	 */
	private int getWidthHint(Map<String, String> parameters) {
		int widthHint = 400;
		final String value = parameters.get("width");
		if (value != null) {
			try {
				final int width = Integer.parseInt(value);
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
	 * @param parameters
	 *            the servlet parameters.
	 * @return the value of the {@code height} parameter or 400 if it is not
	 *         set.
	 */
	private int getHeightHint(Map<String, String> parameters) {
		int heightHint = 400;
		final String value = parameters.get("height");
		if (value != null) {
			try {
				final int height = Integer.parseInt(value);
				heightHint = height;
			} catch (NumberFormatException ignore) {
				// ignore, just use the default height
			}
		}
		return heightHint;
	}

	/**
	 * Sets up the passed chart object with reasonable tooltip generators.
	 * 
	 * @param chart
	 *            the chart to setup tooltip generators on.
	 */
	private void setupTooltips(final JFreeChart chart) {
		final Plot plot = chart.getPlot();
		if (plot instanceof CategoryPlot) {
			System.out.println("plot is a CATEGORY PLOT adding tooltips");
			CategoryPlot cplot = (CategoryPlot) plot;
			cplot.getRenderer().setBaseToolTipGenerator(
					new StandardCategoryToolTipGenerator());
		}
	}

	/**
	 * The singleton instance.
	 */
	private static final ChartCache INSTANCE = new ChartCache();

	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return the singleton instance of this class.
	 */
	public static ChartCache getInstance() {
		return INSTANCE;
	}

	private ChartCache() {
		// singleton
	}
}

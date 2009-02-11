package com.surelogic.sierra.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;

import com.surelogic.common.FileUtility;
import com.surelogic.common.Sweepable;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.chart.IDatabasePlot;
import com.surelogic.sierra.chart.PlotSize;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerQuery;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.servlets.ServletUtility;

/**
 * This class is thread safe.
 */
public final class ChartCache implements Sweepable {

	public void sendPng(final Ticket ticket, final HttpServletResponse response)
			throws ServletException, IOException {
		final File png = getPngFileFor(ticket);
		checkAndUpdateCache(ticket);
		ServletUtility
				.sendFileToHttpServletResponse(png, response, "image/png");
	}

	public void sendMap(final Ticket ticket, final HttpServletResponse response)
			throws ServletException, IOException {
		final File map = getMapFileFor(ticket);
		checkAndUpdateCache(ticket);
		ServletUtility.sendFileToHttpServletResponse(map, response,
				"text/plain");
	}

	public void sendMapTo(final Ticket ticket, final Writer out)
			throws ServletException, IOException {
		final File map = getMapFileFor(ticket);
		checkAndUpdateCache(ticket);
		ServletUtility.sendFileTo(map, out);
	}

	private static final String CHART_CACHE_FILE_PREFIX = "chart-";

	private File getPngFileFor(final Ticket ticket) {
		return new File(FileUtility.getSierraTeamServerCacheDirectory(),
				CHART_CACHE_FILE_PREFIX + ticket.getUUID().toString() + ".png");
	}

	private File getMapFileFor(final Ticket ticket) {
		return new File(FileUtility.getSierraTeamServerCacheDirectory(),
				CHART_CACHE_FILE_PREFIX + ticket.getUUID().toString() + ".map");
	}

	private File getRevFileFor(final Ticket ticket) {
		return new File(FileUtility.getSierraTeamServerCacheDirectory(),
				CHART_CACHE_FILE_PREFIX + ticket.getUUID().toString() + ".rev");
	}

	private void checkAndUpdateCache(final Ticket ticket)
			throws ServletException {
		boolean createOrUpdateCacheFiles = true;
		final File file = getRevFileFor(ticket);
		/*
		 * Does a cached file exist?
		 */
		if (file.exists()) {
			/*
			 * Is it OK to use cached file?
			 */
			try {
				final BufferedReader reader = new BufferedReader(
						new FileReader(file));
				try {
					try {
						final long rev = Long.valueOf(reader.readLine());
						final long lastRevision = ConnectionFactory
								.getInstance().withReadUncommitted(
										new RevisionQuery());

						createOrUpdateCacheFiles = lastRevision > rev;
					} catch (final NumberFormatException e) {
						createOrUpdateCacheFiles = true;
					}
				} finally {
					reader.close();
				}
			} catch (final Exception e) {
				throw new IllegalStateException(e);
			}
		}
		if (createOrUpdateCacheFiles) {
			createCacheFiles(ticket);
		}
	}

	private void createCacheFiles(final Ticket ticket) throws ServletException {
		SLLogger.getLogger().log(Level.FINE,
				"Creating chart files for ticket " + ticket);
		final ReportSettings report = ticket.getReport();
		final String type = report.getReportUuid();
		if (type == null) {
			throw new ServletException(I18N.err(96, ticket.toString()));
		}
		final IDatabasePlot plotter = getPlotter(type);

		ConnectionFactory.getInstance().withReadOnly(
				new ServerTransaction<Void>() {
					public Void perform(final Connection conn,
							final Server server) throws SQLException {
						try {
							final PlotSize mutableSize = new PlotSize(
									getWidthHint(report), getHeightHint(report));
							final JFreeChart chart = plotter.plot(mutableSize,
									report, conn);

							setupTooltips(chart);
							final ChartRenderingInfo info = new ChartRenderingInfo(
									new StandardEntityCollection());
							/*
							 * Output PNG image file.
							 */
							final File pngFile = getPngFileFor(ticket);
							final OutputStream pngWriter = new FileOutputStream(
									pngFile);
							ChartUtilities.writeChartAsPNG(pngWriter, chart,
									mutableSize.getWidth(), mutableSize
											.getHeight(), info, true, 9);
							pngWriter.close();

							/*
							 * Output image map file.
							 */
							final File mapFile = getMapFileFor(ticket);
							final PrintWriter mapWriter = new PrintWriter(
									mapFile);
							ChartUtilities.writeImageMap(mapWriter, "map"
									+ ticket.getUUID(), info, false);
							mapWriter.close();

							/*
							 * Output revision file.
							 */
							final File revFile = getRevFileFor(ticket);
							final PrintWriter revWriter = new PrintWriter(
									revFile);
							revWriter.println(new RevisionQuery().perform(
									new ConnectionQuery(conn), server));
							revWriter.close();
							return null;
						} catch (final IOException e) {
							final SQLException sqle = new SQLException();
							sqle.initCause(e);
							throw sqle;
						}
					}
				});
	}

	private static class RevisionQuery implements ServerQuery<Long> {
		public Long perform(final Query q, final Server s) {
			return q.statement("Revision.maxRevision",
					new ResultHandler<Long>() {
						public Long handle(final Result r) {
							for (final Row row : r) {
								return row.nextLong();
							}
							return -1L;
						}
					}).call();
		}
	}

	/**
	 * All access to this map is protected by a lock on the object.
	 */
	private final Map<String, IDatabasePlot> f_typeToPlotter = new HashMap<String, IDatabasePlot>();

	private IDatabasePlot getPlotter(final String type) throws ServletException {
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
				} catch (final ClassCastException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				} catch (final InstantiationException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				} catch (final IllegalAccessException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				} catch (final ClassNotFoundException e) {
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
	 * @param report
	 *            the servlet parameters.
	 * @return the value of the {@code width} parameter or 400 if it is not set.
	 */
	private int getWidthHint(final ReportSettings report) {
		int widthHint = 400;
		final String param = report.getSettingValue("width", 0);
		if (param != null) {
			try {
				final int width = Integer.parseInt(param);
				widthHint = width;
			} catch (final NumberFormatException ignore) {
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
	private int getHeightHint(final ReportSettings report) {
		int heightHint = 400;
		final String param = report.getSettingValue("height", 0);
		if (param != null) {
			try {
				final int height = Integer.parseInt(param);
				heightHint = height;
			} catch (final NumberFormatException ignore) {
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
			final CategoryPlot cplot = (CategoryPlot) plot;
			int index = 0;
			final StandardCategoryToolTipGenerator ttg = new StandardCategoryToolTipGenerator();
			while (true) {
				/*
				 * This is a bad API, there should be a way to get all the
				 * renders without this mess. TODO: File a bug with JFreeChart.
				 */
				final CategoryItemRenderer r = cplot.getRenderer(index++);
				if (r == null) {
					break;
				}
				r.setBaseToolTipGenerator(ttg);
			}
		}
		// TODO more needs to be added for all the different kinds of plots.
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

	public void periodicSweep() {
		// TODO clear out the file cache and our references to it.
	}
}

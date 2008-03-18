package com.surelogic.sierra.chart.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.servlet.DisplayChart;
import org.jfree.chart.servlet.ServletUtilities;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;

/**
 * This singleton class manages tickets used for chart caching within and across
 * sessions. It tries to share tickets across sessions safely by using a node
 * ticket list.
 * <p>
 * This class is thread safe.
 * <p>
 * <i>Implementation note:</i> Any access to the node or session ticket sets
 * requires a lock on {@code this}.
 */
public final class Attendant {

	/**
	 * The set of tickets cached on this node. This is intended to allow sharing
	 * between sessions as best we can.
	 * <p>
	 * TODO: This class grows forever, this is bad and needs to be fixed.
	 */
	public final Set<Ticket> f_nodeTickets = new HashSet<Ticket>();

	/**
	 * Gets or creates a {@link Ticket} object for the passed set of parameters.
	 * This ticket object may be used throughout the lifetime of the passed
	 * session.
	 * 
	 * @param parameters
	 *            the set of parameters to provide a ticket for.
	 * @param session
	 *            the session the {@link Ticket} object is associated with.
	 * @return the {@link Ticket} object.
	 */
	public Ticket getTicket(final Map<String, String> parameters,
			final HttpSession session) {
		if (parameters == null)
			throw new IllegalArgumentException(I18N.err(44, "parameters"));
		if (session == null)
			throw new IllegalArgumentException(I18N.err(44, "session"));

		synchronized (this) {
			Set<Ticket> tickets = getOrCreateSessionTickets(session);
			/*
			 * This set of tickets could have just migrated over from another
			 * machine in a cluster. So we need to add all these tickets to this
			 * node.
			 */
			f_nodeTickets.addAll(tickets);
			/*
			 * Do we have a ticket for this set of parameters?
			 */
			for (Ticket ticket : tickets) {
				if (ticket.getParameters().equals(parameters)) {
					/*
					 * Yes, return the ticket.
					 */
					return ticket;
				}
			}
			/*
			 * No, check this node.
			 */
			for (Ticket ticket : f_nodeTickets) {
				if (ticket.getParameters().equals(parameters)) {
					/*
					 * Yes, add the ticket to the session and return the ticket.
					 */
					tickets.add(ticket);
					return ticket;
				}
			}
			/*
			 * Double No, create the ticket.
			 */
			final Ticket ticket = new Ticket(parameters);
			tickets.add(ticket);
			f_nodeTickets.add(ticket);
			return ticket;
		}
	}

	/**
	 * Gets the {@link Ticket} object associated with the passed session with
	 * the given universally unique identifier (UUID).
	 * 
	 * @param uuid
	 *            the universally unique identifier (UUID) of the desired
	 *            ticket.
	 * @param session
	 *            the session the {@link Ticket} object is associated with.
	 * @return the {@link Ticket} object, or {@code null} if none.
	 */
	public Ticket getTicket(final UUID uuid, final HttpSession session) {
		if (uuid == null)
			throw new IllegalArgumentException(I18N.err(44, "uuid"));
		if (session == null)
			throw new IllegalArgumentException(I18N.err(44, "session"));

		synchronized (this) {
			Set<Ticket> tickets = getOrCreateSessionTickets(session);
			for (Ticket ticket : tickets) {
				if (ticket.getUUID().equals(uuid)) {
					return ticket;
				}
			}
		}
		return null;
	}

	public void sendPNG(final Ticket ticket, final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		final File png = getPNGFileFor(ticket);
		boolean createCacheFiles = true;
		/*
		 * Does a cached file exist?
		 */
		if (png.exists()) {
			/*
			 * Is it OK to use cached file?
			 */
			final Date modified = new Date(png.lastModified());
			// TODO add check with database.
			createCacheFiles = false;
		}
		if (createCacheFiles) {
			// TODO create the cache files and make sure png exists.
		}
		sendCacheFile(png, response, "image/png");
	}

	public void sendMAP(final Ticket ticket, final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

	}

	/**
	 * Gets or creates the set of tickets associated with the passed session.
	 * <p>
	 * Callers must be holding a lock on {@code this}.
	 * 
	 * @param session
	 *            the session to get or create the set of tickets for.
	 * @return a set of tickets (never {@code null}).
	 */
	@SuppressWarnings("unchecked")
	private Set<Ticket> getOrCreateSessionTickets(final HttpSession session) {

		final String TICKET_ID = "com.surelogic.chart.tickets";

		Set<Ticket> result = (Set<Ticket>) session.getAttribute(TICKET_ID);
		if (result == null) {
			result = new HashSet<Ticket>();
			session.setAttribute(TICKET_ID, result);
		}
		return result;
	}

	/**
	 * Saves the chart as a PNG format file in the temporary directory and
	 * populates the {@link ChartRenderingInfo} object which can be used to
	 * generate an HTML image map.
	 * 
	 * @param chart
	 *            the chart to be saved (<code>null</code> not permitted).
	 * @param width
	 *            the width of the chart.
	 * @param height
	 *            the height of the chart.
	 * @param info
	 *            the ChartRenderingInfo object to be populated (<code>null</code>
	 *            permitted).
	 * @param session
	 *            the non-null HttpSession of the client.
	 * 
	 * @return The filename of the chart saved in the temporary directory.
	 * 
	 * @throws IOException
	 *             if there is a problem saving the file.
	 */
	public String cacheChart(JFreeChart chart, int width, int height,
			ChartRenderingInfo info, HttpSession session) throws IOException {

		// if (chart == null) {
		// throw new IllegalArgumentException("Null 'chart' argument.");
		// }
		// ServletUtilities.createTempDir();
		// String prefix = ServletUtilities.tempFilePrefix;
		// if (session == null) {
		// prefix = ServletUtilities.tempOneTimeFilePrefix;
		// }
		// File tempFile = File.createTempFile(prefix, ".png", new File(System
		// .getProperty("java.io.tmpdir")));
		// ChartUtilities.saveChartAsPNG(tempFile, chart, width, height, info);
		// if (session != null) {
		// ServletUtilities.registerChartForDeletion(tempFile, session);
		// }
		// return tempFile.getName();
		return null;
	}

	public static final String CHART_CACHE_FILE_PREFIX = "chart-";

	public File getPNGFileFor(final Ticket ticket) {
		return new File(FileUtility.getSierraTeamServerCacheDirectory()
				+ File.separator + CHART_CACHE_FILE_PREFIX
				+ ticket.getUUID().toString() + ".png");
	}

	public File getMAPFileFor(final Ticket ticket) {
		return new File(FileUtility.getSierraTeamServerCacheDirectory()
				+ File.separator + CHART_CACHE_FILE_PREFIX
				+ ticket.getUUID().toString() + ".map");
	}

	/**
	 * Binary streams the specified file to the HTTP response in 1KB chunks.
	 * 
	 * @param file
	 *            the file to be streamed.
	 * @param response
	 *            the HTTP response object.
	 * @param mimeType
	 *            the mime type of the file, {@code null} is allowed.
	 * 
	 * @throws IOException
	 *             if there is an I/O problem.
	 */
	private void sendCacheFile(File file, HttpServletResponse response,
			String mimeType) throws IOException {

		if (file.exists()) {
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(file));

			// Set HTTP headers
			if (mimeType != null) {
				response.setHeader("Content-Type", mimeType);
			}
			response.setHeader("Content-Length", String.valueOf(file.length()));
			SimpleDateFormat sdf = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			response.setHeader("Last-Modified", sdf.format(new Date(file
					.lastModified())));

			BufferedOutputStream bos = new BufferedOutputStream(response
					.getOutputStream());
			byte[] input = new byte[1024];
			boolean eof = false;
			while (!eof) {
				int length = bis.read(input);
				if (length == -1) {
					eof = true;
				} else {
					bos.write(input, 0, length);
				}
			}
			bos.flush();
			bis.close();
			bos.close();
		} else {
			throw new FileNotFoundException(I18N
					.err(40, file.getAbsolutePath()));
		}
	}

	/**
	 * The singleton instance.
	 */
	private static final Attendant INSTANCE = new Attendant();

	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return the singleton instance of this class.
	 */
	public static Attendant getInstance() {
		return INSTANCE;
	}

	private Attendant() {
		// singleton
	}
}

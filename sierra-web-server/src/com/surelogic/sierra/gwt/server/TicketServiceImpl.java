package com.surelogic.sierra.gwt.server;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.cache.Attendant;
import com.surelogic.sierra.cache.ChartCache;
import com.surelogic.sierra.cache.TableCache;
import com.surelogic.sierra.gwt.client.data.ImageMapData;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Ticket;
import com.surelogic.sierra.gwt.client.service.TicketService;

public class TicketServiceImpl extends RemoteServiceServlet implements
		TicketService {
	private static final long serialVersionUID = -7681941332780937563L;

	private static final Logger log = SLLogger
			.getLoggerFor(TicketServiceImpl.class);

	public Result<Ticket> getTicket(final ReportSettings report) {
		final Ticket ticket = new Ticket(Attendant.getInstance().getTicket(
				report, getThreadLocalRequest().getSession()).getUUID()
				.toString());
		log.log(Level.FINE, "Ticket " + ticket.getUUID() + " created.");
		return Result.success(ticket);
	}

	public Result<ImageMapData> getImageMap(final Ticket ticket) {
		final StringWriter out = new StringWriter();
		try {
			ChartCache.getInstance().sendMapTo(
					Attendant.getInstance().getTicket(
							UUID.fromString(ticket.getUUID()),
							getThreadLocalRequest().getSession()), out);
			return Result.success(new ImageMapData(out.toString()));
		} catch (final ServletException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (final IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return Result.failure(
				"Error retrieving image map for ticket " + ticket, null);
	}

	public Result<ReportTable> getReportTable(final Ticket ticket) {
		try {
			return Result.success(TableCache.getInstance().getReportTable(
					Attendant.getInstance().getTicket(
							UUID.fromString(ticket.getUUID()),
							getThreadLocalRequest().getSession())));
		} catch (final ServletException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (final IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return Result.failure(
				"Error retrieving image map for ticket " + ticket, null);
	}

	public Result<ReportTable> getReportTable(final ReportSettings r) {
		final Ticket ticket = new Ticket(Attendant.getInstance().getTicket(r,
				getThreadLocalRequest().getSession()).getUUID().toString());
		return getReportTable(ticket);
	}

}

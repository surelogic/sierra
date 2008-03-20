package com.surelogic.sierra.servlets.chart;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.chart.cache.Attendant;
import com.surelogic.sierra.chart.cache.ChartCache;
import com.surelogic.sierra.chart.cache.Ticket;
import com.surelogic.sierra.servlets.ServletUtility;

public class PNGServlet extends AbstractChartServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		final Map<String, String> parameters = ServletUtility
				.launderRequestParameters(request);
		final String uuidString = parameters.get("ticket");
		if (uuidString == null) {
			throw new ServletException(I18N.err(81));
		}
		final UUID uuid = UUID.fromString(uuidString);
		final Ticket ticket = Attendant.getInstance().getTicket(uuid,
				request.getSession());
		// TODO ticket could be null!
		SLLogger.log(Level.FINE, "PNGServlet called for " + ticket);
		ChartCache.getInstance().sendPng(ticket, response);
	}

	private static final long serialVersionUID = 5040888860303280445L;
}

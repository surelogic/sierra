package com.surelogic.sierra.servlets.chart;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.chart.cache.Attendant;
import com.surelogic.sierra.chart.cache.Ticket;
import com.surelogic.sierra.servlets.ServletUtility;

public final class TicketAttendantServlet extends AbstractChartServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		final Map<String, String> parameters = ServletUtility
				.launderRequestParameters(request);
		final Ticket ticket = Attendant.getInstance().getTicket(parameters,
				request.getSession());
		final String uuid = ticket.getUUID().toString();
		SLLogger.log(Level.FINE, "TicketAttendantServlet created a ticket "
				+ ticket);

		final PrintWriter out = response.getWriter();
		out
				.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/2002/REC-xhtml1-20020801/DTD/xhtml1-strict.dtd\">");
		out
				.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
		out.println("<head><title>Got A Ticket</title></head>");
		out.println("<body><h3>Got A Ticket</h3>");
		out.println("<p>The ticket for the requested chart is: " + uuid
				+ "</p>");
		out.println("<p>");
		out
				.println("<img src=\"png?ticket="
						+ uuid
						+ "\" style=\"border: none;\" alt=\"A Chart\" usemap=\"map?ticket="
						+ uuid + "#map\" />");
		out.println("</p>");
		out.println("</body></html>");
		out.println("");

	}

	private static final long serialVersionUID = 775592918230573067L;
}

package com.surelogic.sierra.servlets.chart;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.sierra.chart.cache.Attendant;
import com.surelogic.sierra.chart.cache.Ticket;
import com.surelogic.sierra.servlets.ServletUtility;

public final class TicketAttendantServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		final Map<String, String> parameters = ServletUtility
				.launderRequestParameters(request);
		final Ticket ticket = Attendant.getInstance().getTicket(parameters,
				request.getSession());
		final String uuid = ticket.getUUID().toString();

		final PrintWriter out = response.getWriter();

		out.println("<html><head><title>Got A Ticket</title></head>");
		out.println("<body><h3>Got A Ticket</h3>");
		out.println("<p>The ticket for the requested chart is: " + uuid
				+ "</p>");
		out.println("<br>");
		out
				.println("<img src=\"./chart/png?ticket="
						+ uuid
						+ "\" style=\"border: none;\" alt=\"A Chart\" usemap=\"./chart/map?ticket="
						+ uuid + "#map\"/>");
		out.println("</body></html>");
		out.println("");

	}

	private static final long serialVersionUID = 775592918230573067L;
}

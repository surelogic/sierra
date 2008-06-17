package com.surelogic.sierra.servlets.table;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.sierra.cache.Attendant;
import com.surelogic.sierra.cache.TableCache;
import com.surelogic.sierra.cache.Ticket;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.ReportTable.ColumnData;
import com.surelogic.sierra.servlets.ServletUtility;

public class TableServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4222630876852926113L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		final Report report = ServletUtility
				.launderRequestParametersAsReport(req);
		final Ticket ticket = Attendant.getInstance().getTicket(report,
				req.getSession());
		final ReportTable r = TableCache.getInstance().getReportTable(ticket);
		final PrintWriter pw = resp.getWriter();
		pw.write("<table>");
		pw.write("<tr>");
		final List<ColumnData> cols = r.getColumns();
		if (!cols.isEmpty()) {
			pw.write("<colgroup>");
			for (final ColumnData col : cols) {
				pw.write("<col class=\"" + col.getCSS() + "\" />");
			}
			pw.write("</colgroup>");
		}
		for (final String header : r.getHeaders()) {
			pw.write("<th>" + header + "</th>");
		}
		pw.write("</tr>");
		for (final List<String> row : r.getData()) {
			pw.write("<tr>");
			for (final String col : row) {
				pw.write("<td>" + col + "</td>");
			}
			pw.write("</tr>");
		}
		pw.write("</table>");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

}

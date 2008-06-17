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

	public static final String OUTPUT_FORMAT_XML = "xml";
	public static final String OUTPUT_FORMAT_HTML = "html";
	public static final String OUTPUT_FORMAT_JSON = "json";
	
	private String outputFormat = OUTPUT_FORMAT_XML;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4222630876852926113L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		final PrintWriter pw = resp.getWriter();
		 
		try {
			final Report report = ServletUtility
					.launderRequestParametersAsReport(req);
			final Ticket ticket = Attendant.getInstance().getTicket(report,
					req.getSession());
			final ReportTable r = TableCache.getInstance().getReportTable(ticket);			
			if(OUTPUT_FORMAT_HTML.equals(outputFormat)) {
				resp.setContentType("text/html");
				writeHtml(pw, r);
			}
			else if(OUTPUT_FORMAT_JSON.equals(outputFormat)) {
				resp.setContentType("text/plain");
				writeJson(pw, r);
			}
			else {
				resp.setContentType("text/xml");
				pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writeXml(pw, r);
			}
			pw.close();
		}
		catch(Exception e) {
			throw new ServletException(e);
		}
		finally {
			pw.close();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	private void writeXml(final PrintWriter pw, final ReportTable r) throws Exception {
		final List<String> cols = r.getHeaders();
		pw.write("<table>");		
		int i=0;
		for (final List<String> row : r.getData()) {
			pw.append("<row id=\"row").append(Integer.toString(i++)).append("\">");
			int j=0;
			for (final String data : row) {
				final String col = cols.get(j++).replaceAll("\\s", "");
				pw.append("<").append(col).append(">").append(data).append("</").append(col).append(">");
			}
			pw.write("</row>");
		}
		pw.write("</table>");
	}
	
	private void writeJson(final PrintWriter pw, final ReportTable r) throws Exception {
		throw new Exception("writeJson() is not supported yet");
	}
	
	private void writeHtml(final PrintWriter pw, final ReportTable r) throws Exception {
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
}

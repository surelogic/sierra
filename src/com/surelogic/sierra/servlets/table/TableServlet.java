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
import com.surelogic.sierra.gwt.client.data.ColumnData;
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
		
		final StringBuffer buffer = new StringBuffer(256);
		 
		try {
			final Report report = ServletUtility
					.launderRequestParametersAsReport(req);
			final Ticket ticket = Attendant.getInstance().getTicket(report,
					req.getSession());
			final ReportTable r = TableCache.getInstance().getReportTable(ticket);			
			if(OUTPUT_FORMAT_HTML.equals(outputFormat)) {
				resp.setContentType("text/html");
				writeHtml(buffer, r);
			}
			else if(OUTPUT_FORMAT_JSON.equals(outputFormat)) {
				resp.setContentType("text/plain");
				writeJson(buffer, r);
			}
			else {
				resp.setContentType("text/xml");
				buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				writeXml(buffer, r);
			}

			// I need to set this content length for some clients to work properly;
			// an alternative is to say: Transfer-Encoding: chunked, but I don't want to do that
			resp.setContentLength(buffer.length());

			final PrintWriter out = new PrintWriter(resp.getOutputStream());
			out.write(buffer.toString());
			out.close();
			
		}
		catch(Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	private void writeXml(final StringBuffer buffer, final ReportTable r) throws Exception {
		final List<String> cols = r.getHeaders();
		buffer.append("<table>");		
		int i=0;
		for (final List<String> row : r.getData()) {
			buffer.append("<row id=\"row").append(Integer.toString(i++)).append("\">");
			int j=0;
			for (final String data : row) {
				final String col = cols.get(j++).replaceAll("\\s", "");
				buffer.append("<").append(col).append(">").append(data).append("</").append(col).append(">");
			}
			buffer.append("</row>");
		}
		buffer.append("</table>");
	}
	
	private void writeJson(final StringBuffer buffer, final ReportTable r) throws Exception {
		throw new Exception("writeJson() is not supported yet");
	}
	
	private void writeHtml(final StringBuffer buffer, final ReportTable r) throws Exception {
		buffer.append("<table>");
		buffer.append("<tr>");
		final List<ColumnData> cols = r.getColumns();
		if (!cols.isEmpty()) {
			buffer.append("<colgroup>");
			for (final ColumnData col : cols) {
				buffer.append("<col class=\"" + col.getCSS() + "\" />");
			}
			buffer.append("</colgroup>");
		}
		for (final String header : r.getHeaders()) {
			buffer.append("<th>" + header + "</th>");
		}
		buffer.append("</tr>");
		for (final List<String> row : r.getData()) {
			buffer.append("<tr>");
			for (final String col : row) {
				buffer.append("<td>" + col + "</td>");
			}
			buffer.append("</tr>");
		}
		buffer.append("</table>");
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}
}

package com.surelogic.sierra.client.eclipse.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.surelogic.common.xml.Entities;
import com.surelogic.sierra.tool.message.Importance;

public class ExportFindingSetInHTMLFormatJob extends ExportFindingSetJob {

	PrintWriter writer;

	public ExportFindingSetInHTMLFormatJob(final String listOfFindingsQuery,
			final File file) {
		super(listOfFindingsQuery, file);
	}

	@Override
	protected void closeOutput() throws Exception {
		writer.println("</tbody></table></body></html>");
		writer.close();
	}

	@Override
	protected void openOutput() throws Exception {
		writer = new PrintWriter(f_file);
		writer.println("<html><head>");
		addStyle(writer);
		writer.println("</head><body><table>");
		writer
				.println("<thead><th>Project</th><th>Package</th><th>Class</th><th>Line</th><th>Finding Type</th><th>Importance</th><th>Tool</th><th>Summary</th></thead><tbody>");
	}

	@Override
	protected void outputFinding(final String summary,
			final Importance importance, final long findingId,
			final String projectName, final String packageName,
			final String typeName, final int lineNumber,
			final String findingTypeName, final String toolName)
			throws Exception {
		final StringBuilder b = new StringBuilder();
		b.append("<tr>");
		cell(projectName, b);
		cell(packageName, b);
		cell(typeName, b);
		cell(Integer.toString(lineNumber), b);
		cell(findingTypeName, b);
		cell(importance.toStringSentenceCase(), b);
		cell(toolName, b);
		cell(summary, b);
		b.append("</tr>");
		writer.println(b);
	}

	private void cell(final String val, final StringBuilder b) {
		b.append("<td>");
		Entities.addEscaped(val, b);
		b.append("</td>");
	}

	/**
	 * ppp Optional method. May be called to add a style section to the header.
	 * 
	 * @param writer
	 * @return
	 */
	protected void addStyle(final PrintWriter writer) {
		writer.println("<style>");
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(
								"/com/surelogic/common/adhoc/jobs/style.css")));
		try {
			String str = in.readLine();
			while (str != null) {
				writer.println(str);
				str = in.readLine();
			}
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
		writer.println("</style>");
	}
}

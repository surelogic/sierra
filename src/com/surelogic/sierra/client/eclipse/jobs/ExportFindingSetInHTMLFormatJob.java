package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
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
		writer.println("</table></html>");
		writer.close();
	}

	@Override
	protected void openOutput() throws Exception {
		writer = new PrintWriter(f_file);
		writer.println("<html><table>");
		writer
				.println("<th>Project</th><th>Package</th><th>Class</th><th>Line</th><th>FindingType</th><th>FindingCategory</th><th>Importance</th><th>Tool</th><th>Summary</th>");
	}

	@Override
	protected void outputFinding(final String summary,
			final Importance importance, final long findingId,
			final String projectName, final String packageName,
			final String typeName, final int lineNumber,
			final String findingTypeName, final String categoryName,
			final String toolName) throws Exception {
		final StringBuilder b = new StringBuilder();
		b.append("<tr>");
		cell(summary, b);
		cell(importance.toStringSentenceCase(), b);
		cell(Long.toString(findingId), b);
		cell(projectName, b);
		cell(packageName, b);
		cell(typeName, b);
		cell(Integer.toString(lineNumber), b);
		cell(findingTypeName, b);
		cell(categoryName, b);
		cell(toolName, b);
		b.append("</tr>");
		writer.println(b);
	}

	private void cell(final String val, final StringBuilder b) {
		b.append("<td>");
		Entities.addEscaped(val, b);
		b.append("</td>");
	}
}

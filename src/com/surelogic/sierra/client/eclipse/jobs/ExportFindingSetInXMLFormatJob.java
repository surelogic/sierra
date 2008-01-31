package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.io.PrintWriter;

import com.surelogic.common.xml.Entities;
import com.surelogic.sierra.tool.message.Importance;

public final class ExportFindingSetInXMLFormatJob extends ExportFindingSetJob {

	public ExportFindingSetInXMLFormatJob(final String listOfFindingsQuery,
			File file) {
		super(listOfFindingsQuery, file);
	}

	PrintWriter f_out;

	private void o(final String s) {
		f_out.println(s);
	}

	@Override
	protected void openOutput() throws Exception {
		f_out = new PrintWriter(f_file);
		o("<findings>");
	}

	@Override
	protected void outputFinding(String summary, Importance importance,
			long findingId, String projectName, String packageName,
			String typeName, int lineNumber, String findingTypeName,
			String categoryName, String toolName) throws Exception {
		StringBuilder b = new StringBuilder();
		b.append("<finding");
		Entities.addAttribute("id", findingId, b);
		b.append('>');
		o(b.toString());

		b = new StringBuilder();
		b.append("<project>");
		Entities.addEscaped(projectName, b);
		b.append("</project>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<package>");
		Entities.addEscaped(packageName, b);
		b.append("</package>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<class>");
		Entities.addEscaped(typeName, b);
		b.append("</class>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<line>");
		b.append(lineNumber);
		b.append("</line>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<finding-type>");
		Entities.addEscaped(findingTypeName, b);
		b.append("</finding-type>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<finding-category>");
		Entities.addEscaped(categoryName, b);
		b.append("</finding-category>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<importance>");
		Entities.addEscaped(importance.toStringSentenceCase(), b);
		b.append("</importance>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<tool>");
		Entities.addEscaped(toolName, b);
		b.append("</tool>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<summary>");
		Entities.addEscaped(summary, b);
		b.append("</summary>");
		o(b.toString());

		o("</finding>");
	}

	@Override
	protected void closeOutput() throws Exception {
		o("</findings>");
		f_out.close();
	}
}

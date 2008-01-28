package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.io.PrintWriter;
import java.util.Set;

import com.surelogic.common.Entities;
import com.surelogic.sierra.jdbc.finding.FindingDetail;

public final class ExportFindingSetInXMLFormatJob extends ExportFindingSetJob {

	public ExportFindingSetInXMLFormatJob(Set<Long> findingIds, File file) {
		super(findingIds, file);
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
	protected void outputFinding(FindingDetail finding) throws Exception {
		StringBuilder b = new StringBuilder();
		b.append("<finding");
		Entities.addAttribute("id", finding.getFindingId(), b);
		b.append('>');
		o(b.toString());

		b = new StringBuilder();
		b.append("<project>");
		Entities.addEscaped(finding.getProjectName(), b);
		b.append("</project>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<package>");
		Entities.addEscaped(finding.getPackageName(), b);
		b.append("</package>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<class>");
		Entities.addEscaped(finding.getClassName(), b);
		b.append("</class>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<line>");
		b.append(finding.getLineOfCode());
		b.append("</line>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<finding-type>");
		Entities.addEscaped(finding.getFindingType(), b);
		b.append("</finding-type>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<finding-category>");
		Entities.addEscaped(finding.getCategory(), b);
		b.append("</finding-category>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<importance>");
		Entities.addEscaped(finding.getImportance().toStringSentenceCase(), b);
		b.append("</importance>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<tool>");
		Entities.addEscaped(finding.getTool(), b);
		b.append("</tool>");
		o(b.toString());

		b = new StringBuilder();
		b.append("<summary>");
		Entities.addEscaped(finding.getSummary(), b);
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

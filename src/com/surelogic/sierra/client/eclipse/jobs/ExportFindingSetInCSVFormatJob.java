package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.io.PrintWriter;
import java.util.Set;

import com.surelogic.sierra.jdbc.finding.FindingDetail;

public final class ExportFindingSetInCSVFormatJob extends ExportFindingSetJob {

	public ExportFindingSetInCSVFormatJob(Set<Long> findingIds, File file) {
		super(findingIds, file);
	}

	PrintWriter f_out;

	@Override
	protected void openOutput() throws Exception {
		f_out = new PrintWriter(f_file);
		f_out.println("Project,Package,Class,Line,"
				+ "FindingType,FindingCategory,Tool,Summary");
	}

	@Override
	protected void outputFinding(FindingDetail finding) throws Exception {
		final StringBuilder b = new StringBuilder();
		b.append('"').append(finding.getProjectName()).append('"');
		b.append(',');
		b.append('"').append(finding.getPackageName()).append('"');
		b.append(',');
		b.append('"').append(finding.getClassName()).append('"');
		b.append(',');
		b.append(finding.getLineOfCode());
		b.append(',');
		b.append('"').append(finding.getFindingType()).append('"');
		b.append(',');
		b.append('"').append(finding.getCategory()).append('"');
		b.append(',');
		b.append('"').append(finding.getTool()).append('"');
		b.append(',');
		b.append('"').append(finding.getSummary()).append('"');

		f_out.println(b.toString());
	}

	@Override
	protected void closeOutput() throws Exception {
		f_out.close();
	}
}

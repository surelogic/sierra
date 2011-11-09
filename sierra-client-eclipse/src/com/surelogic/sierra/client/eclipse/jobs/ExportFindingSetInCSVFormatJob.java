package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.io.PrintWriter;

import com.surelogic.sierra.tool.message.Importance;

public final class ExportFindingSetInCSVFormatJob extends ExportFindingSetJob {

	public ExportFindingSetInCSVFormatJob(final String listOfFindingsQuery,
			final File file) {
		super(listOfFindingsQuery, file);
	}

	PrintWriter f_out;

	@Override
	protected void openOutput() throws Exception {
		f_out = new PrintWriter(f_file);
		f_out.println("Project,Package,Class,Line,"
				+ "FindingType,Importance,Tool,Summary");
	}

	@Override
	protected void outputFinding(final String summary,
			final Importance importance, final long findingId,
			final String projectName, final String packageName,
			final String typeName, final int lineNumber,
			final String findingTypeName, final String toolName)
			throws Exception {
		final StringBuilder b = new StringBuilder();
		b.append('"').append(projectName).append('"');
		b.append(',');
		b.append('"').append(packageName).append('"');
		b.append(',');
		b.append('"').append(typeName).append('"');
		b.append(',');
		b.append(lineNumber);
		b.append(',');
		b.append('"').append(findingTypeName).append('"');
		b.append(',');
		b.append('"').append(importance.toStringSentenceCase()).append('"');
		b.append(',');
		b.append('"').append(toolName).append('"');
		b.append(',');
		b.append('"').append(summary).append('"');

		f_out.println(b.toString());

	}

	@Override
	protected void closeOutput() throws Exception {
		f_out.close();
	}
}

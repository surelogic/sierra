package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.io.PrintWriter;

import com.surelogic.sierra.tool.message.Importance;

public final class ExportFindingSetInCSVFormatJob extends ExportFindingSetJob {

	public ExportFindingSetInCSVFormatJob(final String listOfFindingsQuery,
			File file) {
		super(listOfFindingsQuery, file);
	}

	PrintWriter f_out;

	@Override
	protected void openOutput() throws Exception {
		f_out = new PrintWriter(f_file);
		f_out.println("Project,Package,Class,Line,"
				+ "FindingType,FindingCategory,Importance,Tool,Summary");
	}

	@Override
	protected void outputFinding(String summary, Importance importance,
			long findingId, String projectName, String packageName,
			String typeName, int lineNumber, String findingTypeName,
			String categoryName, String toolName) throws Exception {
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
		b.append('"').append(categoryName).append('"');
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

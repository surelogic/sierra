package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.tool.message.Importance;

public abstract class ExportFindingSetJob extends DatabaseJob {

	protected final String f_listOfFindingsQuery;
	protected final File f_file;

	public ExportFindingSetJob(final String listOfFindingsQuery, final File file) {
		super("Exporting findings to a file");
		if (listOfFindingsQuery == null)
			throw new IllegalArgumentException(
					"listOfFindingsQuery must be non-null");
		if (file == null)
			throw new IllegalArgumentException("file must be non-null");
		f_listOfFindingsQuery = listOfFindingsQuery;
		f_file = file;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Exporting findings to " + f_file.getName(),
				IProgressMonitor.UNKNOWN);
		final String query = f_listOfFindingsQuery;
		try {
			openOutput();
			final Connection c = Data.readOnlyConnection();
			try {
				final Statement st = c.createStatement();
				try {
					if (SLLogger.getLogger().isLoggable(Level.FINE)) {
						SLLogger.getLogger().fine(
								"Export list of findings query: " + query);
					}
					final ResultSet rs = st.executeQuery(query);
					while (rs.next()) {
						String summary = rs.getString(1);
						Importance importance = Importance.valueOf(rs
								.getString(2).toUpperCase());
						long findingId = rs.getLong(3);
						String projectName = rs.getString(4);
						String packageName = rs.getString(5);
						String typeName = rs.getString(6);
						int lineNumber = rs.getInt(7);
						String findingTypeName = rs.getString(8);
						String categoryName = rs.getString(9);
						String toolName = rs.getString(10);
						outputFinding(summary, importance, findingId,
								projectName, packageName, typeName, lineNumber,
								findingTypeName, categoryName, toolName);
					}
				} finally {
					st.close();
				}
			} finally {
				c.close();
			}
			closeOutput();
		} catch (Exception e) {
			return SLStatus.createErrorStatus("Unable to export findings to "
					+ f_file.getName(), e);
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	protected abstract void openOutput() throws Exception;

	protected abstract void outputFinding(String summary,
			Importance importance, long findingId, String projectName,
			String packageName, String typeName, int lineNumber,
			String findingTypeName, String categoryName, String toolName)
			throws Exception;

	protected abstract void closeOutput() throws Exception;
}

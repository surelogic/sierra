package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.tool.message.Importance;

public abstract class ExportFindingSetJob extends AbstractSierraDatabaseJob {

	protected final String f_listOfFindingsQuery;
	protected final File f_file;

	public ExportFindingSetJob(final String listOfFindingsQuery, final File file) {
		super("Exporting findings to a file");
		if (listOfFindingsQuery == null) {
			throw new IllegalArgumentException(
					"listOfFindingsQuery must be non-null");
		}
		if (file == null) {
			throw new IllegalArgumentException("file must be non-null");
		}
		f_listOfFindingsQuery = listOfFindingsQuery;
		f_file = file;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		monitor.beginTask("Exporting findings to " + f_file.getName(),
				IProgressMonitor.UNKNOWN);
		final String query = f_listOfFindingsQuery;
		try {
			openOutput();
			final Connection c = Data.getInstance().readOnlyConnection();
			try {
				final Statement st = c.createStatement();
				try {
					if (SLLogger.getLogger().isLoggable(Level.FINE)) {
						SLLogger.getLogger().fine(
								"Export list of findings query: " + query);
					}
					final ResultSet rs = st.executeQuery(query);
					try {
						while (rs.next()) {
							// FO.SUMMARY, FO.IMPORTANCE, FO.FINDING_ID,
							// FO.PROJECT, FO.PACKAGE, FO.CLASS,
							// FO.LINE_OF_CODE, FO.FINDING_TYPE,
							// FO.FINDING_TYPE_NAME, FO.TOOL, FO.ASSURANCE_TYPE
							final String summary = rs.getString(1);
							final Importance importance = Importance.valueOf(rs
									.getString(2).toUpperCase());
							final long findingId = rs.getLong(3);
							final String projectName = rs.getString(4);
							final String packageName = rs.getString(5);
							final String typeName = rs.getString(6);
							final int lineNumber = rs.getInt(7);
							final String findingTypeName = rs.getString(9);
							final String toolName = rs.getString(10);
							outputFinding(summary, importance, findingId,
									projectName, packageName, typeName,
									lineNumber, findingTypeName, toolName);
						}
					} finally {
						rs.close();
					}
				} finally {
					st.close();
				}
			} finally {
				c.close();
			}
			closeOutput();
		} catch (final Exception e) {
			final int errNo = 47;
			final String msg = I18N.err(errNo, f_file.getName());
			return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	protected abstract void openOutput() throws Exception;

	protected abstract void outputFinding(String summary,
			Importance importance, long findingId, String projectName,
			String packageName, String typeName, int lineNumber,
			String findingTypeName, String toolName) throws Exception;

	protected abstract void closeOutput() throws Exception;
}

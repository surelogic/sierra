package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.sql.Connection;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.finding.FindingDetail;

public abstract class ExportFindingSetJob extends DatabaseJob {

	protected final Set<Long> f_findingIds;
	protected final File f_file;

	public ExportFindingSetJob(final Set<Long> findingIds, final File file) {
		super("Exporting findings to a file");
		if (findingIds == null)
			throw new IllegalArgumentException("findingIds must be non-null");
		if (file == null)
			throw new IllegalArgumentException("file must be non-null");
		f_findingIds = findingIds;
		f_file = file;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Exporting findings to " + f_file.getName(),
				f_findingIds.size());
		try {
			openOutput();
			Connection c = Data.readOnlyConnection();
			try {
				for (Long findingId : f_findingIds) {
					if (findingId != null)
						try {
							outputFinding(findingId, c);
						} catch (Exception e) {
							return SLStatus.createErrorStatus(
									"Export of finding " + findingId + " to "
											+ f_file.getName() + " failed", e);
						}
					monitor.worked(1);
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

	private void outputFinding(long findingId, Connection c) throws Exception {
		final FindingDetail finding = FindingDetail.getDetail(c, findingId);
		if (finding != null)
			outputFinding(finding);
	}

	protected abstract void outputFinding(final FindingDetail finding)
			throws Exception;

	protected abstract void closeOutput() throws Exception;

}

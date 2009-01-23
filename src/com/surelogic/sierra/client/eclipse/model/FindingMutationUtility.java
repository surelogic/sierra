package com.surelogic.sierra.client.eclipse.model;

import java.sql.Connection;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.tool.message.Importance;

public final class FindingMutationUtility {

	private FindingMutationUtility() {
		// no instances
	}

	static abstract class MutationJob extends DatabaseJob {
		public MutationJob(final String name) {
			super(name, Job.INTERACTIVE);
			setSystem(false);
		}

		@Override
		protected final IStatus run(final IProgressMonitor monitor) {
			monitor
					.beginTask("Updating finding data",
							IProgressMonitor.UNKNOWN);
			try {
				final Connection c = Data.getInstance().transactionConnection();
				Exception exc = null;
				try {
					final ClientFindingManager manager = ClientFindingManager
							.getInstance(c);
					updateFindings(monitor, manager);
					c.commit();
					DatabaseHub.getInstance().notifyFindingMutated();
				} catch (final Exception e) {
					c.rollback();
					exc = e;
				} finally {
					try {
						c.close();
					} finally {
						if (exc != null) {
							throw exc;
						}
					}
				}
			} catch (final Exception e) {
				final int errNo = 52;
				final String msg = I18N.err(errNo, getName());
				return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
			}
			monitor.done();
			return Status.OK_STATUS;
		}

		protected abstract void updateFindings(IProgressMonitor monitor,
				ClientFindingManager manager) throws Exception;
	}

	public static void asyncChangeSummary(final long finding_id,
			final String summary) {
		if (summary == null || summary.trim().equals("")) {
			return;
		}

		final Job job = new MutationJob("Changing the summary of finding id "
				+ finding_id + " to \"" + summary + "\"") {
			@Override
			protected void updateFindings(final IProgressMonitor monitor,
					final ClientFindingManager manager) throws Exception {
				manager.changeSummary(finding_id, summary);
			}
		};
		job.schedule();
	}

	public static void asyncComment(final long finding_id, final String comment) {
		if (comment == null || comment.trim().equals("")) {
			return;
		}

		final Job job = new MutationJob("Adding the comment \"" + comment
				+ "\" to finding id " + finding_id) {
			@Override
			protected void updateFindings(final IProgressMonitor monitor,
					final ClientFindingManager manager) throws Exception {
				manager.comment(finding_id, comment);
			}
		};
		job.schedule();
	}

	public static void asyncComment(final Collection<Long> finding_ids,
			final String comment) {
		if (comment == null || comment.trim().equals("")) {
			return;
		}

		final Job job = new MutationJob("Adding the comment \"" + comment
				+ "\" to " + finding_ids.size() + " findings") {
			@Override
			protected void updateFindings(final IProgressMonitor monitor,
					final ClientFindingManager manager) throws Exception {
				for (final Long finding_id : finding_ids) {
					if (finding_id != null) {
						manager.comment(finding_id, comment);
					}
				}
			}
		};
		job.schedule();
	}

	public static void asyncChangeImportance(final long finding_id,
			final Importance from, final Importance to) {
		final Job job = new MutationJob(
				"Changing the importance of finding id " + finding_id
						+ " from " + from + " to " + to) {
			@Override
			protected void updateFindings(final IProgressMonitor monitor,
					final ClientFindingManager manager) throws Exception {
				manager.setImportance(finding_id, to);
			}
		};
		job.schedule();
	}

	public static void asyncChangeImportance(
			final Collection<Long> finding_ids, final Importance to) {
		final Job job = new MutationJob("Changing the importance of "
				+ finding_ids.size() + " finding(s) to "
				+ to.toStringSentenceCase()) {
			@Override
			protected void updateFindings(final IProgressMonitor monitor,
					final ClientFindingManager manager) throws Exception {
				manager.setImportance(finding_ids, to,
						new SLProgressMonitorWrapper(monitor,
								"Updating finding data"));
			}
		};
		job.setUser(true);
		job.schedule();
	}

	public static void asyncFilterFindingTypeFromScans(final long finding_id,
			final String findingTypeName) {
		final Job job = new MutationJob("Filtering out '" + findingTypeName
				+ "' from future scans") {
			@Override
			protected void updateFindings(final IProgressMonitor monitor,
					final ClientFindingManager manager) throws Exception {
				if (!manager.filterFindingTypeFromScans(finding_id,
						new SLProgressMonitorWrapper(monitor,
								"Filter finding type from scans"))) {
					final UIJob uiJob = new UIJob(
							"Scan settings owned by server.") {
						@Override
						public IStatus runInUIThread(
								final IProgressMonitor monitor) {
							MessageDialog
									.openInformation(
											SWTUtility.getShell(),
											"Scan settings owned by server.",
											"This finding belongs to a project whose settings are not managed locally.  You will need to update the project's scan filter on the server.");
							return Status.OK_STATUS;
						}
					};
					uiJob.schedule();
				}
			}
		};
		job.setUser(true);
		job.schedule();
	}

	public static void asyncFilterFindingTypeFromScans(
			final Collection<Long> finding_ids, final String findingTypeName) {
		final Job job = new MutationJob("Filtering out '" + findingTypeName
				+ "' from future scans") {
			@Override
			protected void updateFindings(final IProgressMonitor monitor,
					final ClientFindingManager manager) throws Exception {
				final SLProgressMonitor mon = new SLProgressMonitorWrapper(
						monitor, getName());
				for (final Long finding_id : finding_ids) {
					if (finding_id != null) {
						manager.filterFindingTypeFromScans(finding_id, mon);
					}
				}
			}
		};
		job.setUser(true);
		job.schedule();
	}
}
